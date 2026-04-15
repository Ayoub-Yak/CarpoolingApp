package com.covoiturage.service;

import com.covoiturage.dao.VehiculeDao;
import com.covoiturage.dao.VehiculeDaoImpl;
import com.covoiturage.model.Chauffeur;
import com.covoiturage.model.Vehicule;

import java.util.List;

/**
 * Service de gestion des véhicules.
 */
public class VehiculeService {

    private final VehiculeDao vehiculeDao;

    public VehiculeService() {
        this.vehiculeDao = new VehiculeDaoImpl();
    }

    /**
     * Ajoute un véhicule à un chauffeur.
     */
    public void ajouterVehicule(Chauffeur chauffeur, Vehicule vehicule) {
        vehicule.setProprietaireId(chauffeur.getId());
        vehiculeDao.save(vehicule);
        chauffeur.ajouterVehicule(vehicule);
    }

    /**
     * Retourne les véhicules d'un chauffeur.
     */
    public List<Vehicule> getVehiculesByChauffeur(int chauffeurId) {
        return vehiculeDao.findByProprietaireId(chauffeurId);
    }

    public void update(Vehicule vehicule) {
        vehiculeDao.update(vehicule);
    }

    public void delete(int id) {
        vehiculeDao.delete(id);
    }

    public Vehicule findById(int id) {
        return vehiculeDao.findById(id);
    }
}
