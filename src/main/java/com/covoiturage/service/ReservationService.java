package com.covoiturage.service;

import com.covoiturage.dao.ReservationDao;
import com.covoiturage.dao.ReservationDaoImpl;
import com.covoiturage.dao.TrajetDao;
import com.covoiturage.dao.TrajetDaoImpl;
import com.covoiturage.model.Passager;
import com.covoiturage.model.Paiement;
import com.covoiturage.model.Reservation;
import com.covoiturage.model.Trajet;
import com.covoiturage.model.enums.StatutReservation;
import com.covoiturage.model.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service de gestion des réservations.
 * Implémente la règle des 24h pour les annulations.
 */
public class ReservationService {

    private final ReservationDao reservationDao;
    private final TrajetDao trajetDao;
    private final PaiementService paiementService;
    private final NotificationService notificationService;

    public ReservationService() {
        this.reservationDao = new ReservationDaoImpl();
        this.trajetDao = new TrajetDaoImpl();
        this.paiementService = new PaiementService();
        this.notificationService = new NotificationService();
    }

    /**
     * Crée une réservation pour un passager sur un trajet.
     *
     * @return la réservation créée
     * @throws IllegalStateException si le trajet est complet ou annulé
     */
    public Reservation creerReservation(Passager passager, Trajet trajet) {
        if (trajet.getStatut() != StatutTrajet.PREVU) {
            throw new IllegalStateException("Ce trajet n'est plus disponible.");
        }
        if (trajet.getNbPlacesDisponibles() <= 0) {
            throw new IllegalStateException("Aucune place disponible.");
        }

        Reservation reservation = new Reservation(passager.getId(), trajet.getId());
        reservationDao.save(reservation);

        // Mettre à jour le trajet (décrémenter places, potentiellement COMPLET)
        trajet.ajouterPassager(reservation);
        trajetDao.update(trajet);

        // Notifier le chauffeur
        notificationService.envoyerNotification(trajet.getChauffeurId(), 
            "Nouvelle réservation reçue du passager #" + passager.getId() + " pour le trajet " + trajet.getVilleDepart() + " → " + trajet.getVilleArrivee());

        return reservation;
    }

    /**
     * Confirme une réservation (par le chauffeur).
     */
    public void confirmerReservation(Reservation reservation) {
        reservation.setStatut(StatutReservation.ACCEPTEE);
        reservationDao.update(reservation);

        // Notifier le passager
        notificationService.envoyerNotification(reservation.getPassagerId(), 
            "Votre réservation pour le trajet #" + reservation.getTrajetId() + " a été ACCEPTÉE.");
    }

    /**
     * Refuse une réservation (par le chauffeur).
     */
    public void refuserReservation(Reservation reservation) {
        reservation.setStatut(StatutReservation.REFUSEE);
        reservationDao.update(reservation);

        // Libérer la place sur le trajet
        Trajet trajet = trajetDao.findById(reservation.getTrajetId());
        trajet.retirerPassager(reservation);
        trajetDao.update(trajet);

        // Notifier le passager
        notificationService.envoyerNotification(reservation.getPassagerId(), 
            "Désolé, votre réservation pour le trajet #" + reservation.getTrajetId() + " a été REFUSÉE.");
    }

    /**
     * Annule une réservation par un passager.
     * Applique la règle des 24h :
     * - Si > 24h avant départ : remboursement intégral
     * - Si < 24h avant départ : remboursement partiel seulement
     *
     * @return le pourcentage de remboursement (100 ou 50)
     */
    public int annulerReservationParPassager(Passager passager, Reservation reservation) {
        Trajet trajet = trajetDao.findById(reservation.getTrajetId());

        reservation.setStatut(StatutReservation.ANNULEE);
        reservationDao.update(reservation);

        // Libérer la place sur le trajet
        trajet.retirerPassager(reservation);
        trajetDao.update(trajet);

        // Règle des 24h
        long heuresAvantDepart = ChronoUnit.HOURS.between(LocalDateTime.now(), trajet.getDateHeureDepart());
        int refundPercent = (heuresAvantDepart >= 24) ? 100 : 50;
        
        // Notifier le passager du remboursement
        notificationService.envoyerNotification(passager.getId(), 
            "Réservation annulée. Remboursement de " + refundPercent + "% traité.");
        
        // Notifier le chauffeur
        notificationService.envoyerNotification(trajet.getChauffeurId(), 
            "Le passager #" + passager.getId() + " a annulé sa réservation sur votre trajet.");

        return refundPercent;
    }

    /**
     * Annule toutes les réservations d'un trajet (par le chauffeur).
     * Si < 24h avant départ : pénalité de 20% par passager.
     *
     * @return le montant total de la pénalité
     */
    public double annulerReservationsParChauffeur(Trajet trajet) {
        List<Reservation> reservations = reservationDao.findByTrajetId(trajet.getId());
        double penaliteTotal = 0;

        long heuresAvantDepart = ChronoUnit.HOURS.between(LocalDateTime.now(), trajet.getDateHeureDepart());

        for (Reservation r : reservations) {
            if (r.getStatut() == StatutReservation.ACCEPTEE || r.getStatut() == StatutReservation.EN_ATTENTE) {
                r.setStatut(StatutReservation.ANNULEE);
                reservationDao.update(r);

                // Remboursement intégral du passager.
                Paiement paiement = paiementService.findByReservationId(r.getId());
                double montantBase = trajet.getPrixPlace();
                if (paiement != null) {
                    montantBase = paiement.getMontant();
                    paiementService.rembourser(paiement, montantBase);
                }

                if (heuresAvantDepart < 24) {
                    // Pénalité de 20% par passager
                    double p = montantBase * 0.20;
                    penaliteTotal += p;

                    // Déduire la pénalité supplémentaire du solde chauffeur.
                    paiementService.appliquerPenaliteSurReservation(r.getId(), p);
                }
                
                // Notifier chaque passager
                notificationService.envoyerNotification(r.getPassagerId(), 
                    "Le chauffeur a annulé le trajet #" + trajet.getId() + ". Vous serez remboursé intégralement.");
            }
        }

        if (penaliteTotal > 0) {
            notificationService.envoyerNotification(trajet.getChauffeurId(), 
                "Trajet annulé < 24h. Pénalité totale appliquée : " + String.format("%.2f", penaliteTotal) + " €");
        }

        trajet.setStatut(StatutTrajet.ANNULE);
        trajetDao.update(trajet);

        return penaliteTotal;
    }

    public List<Reservation> getReservationsByPassager(int passagerId) {
        return reservationDao.findByPassagerId(passagerId);
    }

    public List<Reservation> getReservationsByTrajet(int trajetId) {
        return reservationDao.findByTrajetId(trajetId);
    }

    public Reservation findById(int id) {
        return reservationDao.findById(id);
    }

    public void update(Reservation reservation) {
        reservationDao.update(reservation);
    }
}
