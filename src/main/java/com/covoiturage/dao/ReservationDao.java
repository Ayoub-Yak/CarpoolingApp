package com.covoiturage.dao;

import com.covoiturage.model.Reservation;
import java.util.List;

/**
 * Interface DAO pour les opérations sur les réservations.
 */
public interface ReservationDao {

    void save(Reservation reservation);

    Reservation findById(int id);

    List<Reservation> findAll();

    List<Reservation> findByPassagerId(int passagerId);

    List<Reservation> findByTrajetId(int trajetId);

    void update(Reservation reservation);

    void delete(int id);
}
