package com.covoiturage.service;

import com.covoiturage.dao.PaiementDao;
import com.covoiturage.dao.PaiementDaoImpl;
import com.covoiturage.dao.TrajetDao;
import com.covoiturage.dao.TrajetDaoImpl;
import com.covoiturage.dao.UserDao;
import com.covoiturage.dao.UserDaoImpl;
import com.covoiturage.model.Chauffeur;
import com.covoiturage.model.Paiement;
import com.covoiturage.model.Reservation;
import com.covoiturage.model.Trajet;
import com.covoiturage.model.User;
import com.covoiturage.model.enums.StatutPaiement;

import com.covoiturage.dao.ReservationDao;
import com.covoiturage.dao.ReservationDaoImpl;

/**
 * Service de paiement.
 * Suit le cycle : Autorisation → Capture → (Remboursement | Annulation)
 */
public class PaiementService {

    private final PaiementDao paiementDao;
    private final ReservationDao reservationDao;
    private final TrajetDao trajetDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public PaiementService() {
        this.paiementDao = new PaiementDaoImpl();
        this.reservationDao = new ReservationDaoImpl();
        this.trajetDao = new TrajetDaoImpl();
        this.userDao = new UserDaoImpl();
        this.notificationService = new NotificationService();
    }

    /**
     * Autorise un paiement lors de la réservation.
     * Le montant est bloqué mais pas encore capturé.
     */
    public Paiement payer(Reservation reservation, double montant) {
        Paiement paiement = new Paiement(montant, reservation.getId());
        paiement.setStatut(StatutPaiement.AUTORISE);
        paiementDao.save(paiement);
        
        notificationService.envoyerNotification(reservation.getPassagerId(), 
            "Paiement de " + montant + " € autorisé pour votre réservation.");
            
        return paiement;
    }

    /**
     * Capture le paiement quand le chauffeur accepte la réservation.
     */
    public void capturerPaiement(Paiement paiement) {
        if (paiement.getStatut() == StatutPaiement.CAPTURE) {
            return;
        }

        paiement.setStatut(StatutPaiement.CAPTURE);
        paiementDao.update(paiement);

        appliquerVariationSoldeChauffeur(paiement.getReservationId(), paiement.getMontant());
    }

    /**
     * Rembourse un paiement (total ou partiel).
     *
     * @param montant le montant à rembourser
     */
    public void rembourser(Paiement paiement, double montant) {
        StatutPaiement previousStatut = paiement.getStatut();
        paiement.setStatut(StatutPaiement.REMBOURSE);
        paiement.setMontant(montant);
        paiementDao.update(paiement);

        // Si le paiement était déjà capturé, le revenu chauffeur doit être diminué.
        if (previousStatut == StatutPaiement.CAPTURE) {
            appliquerVariationSoldeChauffeur(paiement.getReservationId(), -montant);
        }
        
        Reservation r = reservationDao.findById(paiement.getReservationId());
        if (r != null) {
            notificationService.envoyerNotification(r.getPassagerId(), 
                "Remboursement de " + String.format("%.2f", montant) + " € effectué sur votre compte.");
        }
    }

    /**
     * Annule un paiement.
     */
    public void annulerPaiement(Paiement paiement) {
        StatutPaiement previousStatut = paiement.getStatut();
        paiement.setStatut(StatutPaiement.ANNULE);
        paiementDao.update(paiement);

        if (previousStatut == StatutPaiement.CAPTURE) {
            appliquerVariationSoldeChauffeur(paiement.getReservationId(), -paiement.getMontant());
        }
    }

    /**
     * Applique une pénalité sur le solde du chauffeur lié à la réservation.
     */
    public void appliquerPenaliteSurReservation(int reservationId, double montantPenalite) {
        if (montantPenalite <= 0) {
            return;
        }
        appliquerVariationSoldeChauffeur(reservationId, -Math.abs(montantPenalite));
    }

    private void appliquerVariationSoldeChauffeur(int reservationId, double variation) {
        Reservation reservation = reservationDao.findById(reservationId);
        if (reservation == null) {
            return;
        }

        Trajet trajet = trajetDao.findById(reservation.getTrajetId());
        if (trajet == null) {
            return;
        }

        User user = userDao.findById(trajet.getChauffeurId());
        if (user instanceof Chauffeur) {
            Chauffeur chauffeur = (Chauffeur) user;
            double nouveauSolde = chauffeur.getTotalRevenu() + variation;
            chauffeur.setTotalRevenu(Math.max(0.0, nouveauSolde));
            userDao.update(chauffeur);
        }
    }

    public Paiement findByReservationId(int reservationId) {
        return paiementDao.findByReservationId(reservationId);
    }

    public Paiement findById(int id) {
        return paiementDao.findById(id);
    }
}
