package com.covoiturage.dao;

import com.covoiturage.model.Vehicule;
import java.util.List;

/**
 * Interface DAO pour les opérations sur les véhicules.
 */
public interface VehiculeDao {

    void save(Vehicule vehicule);

    Vehicule findById(int id);

    List<Vehicule> findAll();

    List<Vehicule> findByProprietaireId(int proprietaireId);

    void update(Vehicule vehicule);

    void delete(int id);
}
