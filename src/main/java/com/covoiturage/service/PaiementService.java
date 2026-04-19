package com.covoiturage.service;

import com.covoiturage.dao.PaiementDao;
import com.covoiturage.dao.PaiementDaoImpl;
import com.covoiturage.model.Paiement;
import com.covoiturage.model.Reservation;
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
    private final NotificationService notificationService;

    public PaiementService() {
        this.paiementDao = new PaiementDaoImpl();
        this.reservationDao = new ReservationDaoImpl();
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
        paiement.setStatut(StatutPaiement.CAPTURE);
        paiementDao.update(paiement);
    }

    /**
     * Rembourse un paiement (total ou partiel).
     *
     * @param montant le montant à rembourser
     */
    public void rembourser(Paiement paiement, double montant) {
        paiement.setStatut(StatutPaiement.REMBOURSE);
        paiement.setMontant(montant);
        paiementDao.update(paiement);
        
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
        paiement.setStatut(StatutPaiement.ANNULE);
        paiementDao.update(paiement);
    }

    public Paiement findByReservationId(int reservationId) {
        return paiementDao.findByReservationId(reservationId);
    }

    public Paiement findById(int id) {
        return paiementDao.findById(id);
    }
}
