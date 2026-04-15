package com.covoiturage.service;

import com.covoiturage.dao.ReservationDao;
import com.covoiturage.dao.ReservationDaoImpl;
import com.covoiturage.dao.TrajetDao;
import com.covoiturage.dao.TrajetDaoImpl;
import com.covoiturage.model.Passager;
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

    public ReservationService() {
        this.reservationDao = new ReservationDaoImpl();
        this.trajetDao = new TrajetDaoImpl();
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

        return reservation;
    }

    /**
     * Confirme une réservation (par le chauffeur).
     */
    public void confirmerReservation(Reservation reservation) {
        reservation.setStatut(StatutReservation.ACCEPTEE);
        reservationDao.update(reservation);
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
        if (heuresAvantDepart >= 24) {
            return 100; // Remboursement intégral
        } else {
            return 50; // Remboursement partiel
        }
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

                if (heuresAvantDepart < 24) {
                    // Pénalité de 20% par passager
                    penaliteTotal += trajet.getPrixPlace() * 0.20;
                }
            }
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
