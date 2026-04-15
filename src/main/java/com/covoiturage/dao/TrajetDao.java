package com.covoiturage.dao;

import com.covoiturage.model.Trajet;
import java.util.List;

/**
 * Interface DAO pour les opérations sur les trajets.
 */
public interface TrajetDao {

    void save(Trajet trajet);

    Trajet findById(int id);

    List<Trajet> findAll();

    List<Trajet> findByChauffeurId(int chauffeurId);

    List<Trajet> findByVilleDepart(String villeDepart);

    /**
     * Retourne les trajets avec des places disponibles (statut PREVU).
     */
    List<Trajet> findDisponibles();

    void update(Trajet trajet);

    void delete(int id);
}
