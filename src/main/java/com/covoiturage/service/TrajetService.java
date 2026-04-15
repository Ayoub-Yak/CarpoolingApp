package com.covoiturage.service;

import com.covoiturage.dao.TrajetDao;
import com.covoiturage.dao.TrajetDaoImpl;
import com.covoiturage.model.Chauffeur;
import com.covoiturage.model.Trajet;

import java.util.List;

/**
 * Service de gestion des trajets.
 */
public class TrajetService {

    private final TrajetDao trajetDao;

    public TrajetService() {
        this.trajetDao = new TrajetDaoImpl();
    }

    /**
     * Propose un nouveau trajet par un chauffeur.
     */
    public void proposerTrajet(Chauffeur chauffeur, Trajet trajet) {
        trajet.setChauffeurId(chauffeur.getId());
        trajetDao.save(trajet);
        chauffeur.ajouterTrajet(trajet);
    }

    /**
     * Clôture un trajet (passage en EN_COURS).
     */
    public void clorerTrajet(Chauffeur chauffeur, Trajet trajet) {
        trajet.setStatut(com.covoiturage.model.enums.StatutTrajet.EN_COURS);
        trajetDao.update(trajet);
    }

    /**
     * Annule un trajet proposé par le chauffeur.
     */
    public void annulerTrajetParChauffeur(Chauffeur chauffeur, Trajet trajet) {
        trajet.setStatut(com.covoiturage.model.enums.StatutTrajet.ANNULE);
        trajetDao.update(trajet);
    }

    /**
     * Retourne tous les trajets disponibles (PREVU avec des places).
     */
    public List<Trajet> getTrajetsDisponibles() {
        return trajetDao.findDisponibles();
    }

    /**
     * Retourne les trajets d'un chauffeur.
     */
    public List<Trajet> getTrajetsByChauffeur(int chauffeurId) {
        return trajetDao.findByChauffeurId(chauffeurId);
    }

    /**
     * Retourne tous les trajets.
     */
    public List<Trajet> getAllTrajets() {
        return trajetDao.findAll();
    }

    public Trajet findById(int id) {
        return trajetDao.findById(id);
    }

    public void update(Trajet trajet) {
        trajetDao.update(trajet);
    }
}
