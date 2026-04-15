package com.covoiturage.dao;

import com.covoiturage.model.Paiement;
import java.util.List;

/**
 * Interface DAO pour les opérations sur les paiements.
 */
public interface PaiementDao {

    void save(Paiement paiement);

    Paiement findById(int id);

    List<Paiement> findAll();

    Paiement findByReservationId(int reservationId);

    void update(Paiement paiement);

    void delete(int id);
}
