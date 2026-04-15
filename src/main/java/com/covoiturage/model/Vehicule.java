package com.covoiturage.model;

/**
 * Représente un véhicule appartenant à un chauffeur.
 */
public class Vehicule {

    private int id;
    private String marque;
    private String modele;
    private String immatriculation;
    private int placesDisponibles;
    private int proprietaireId; // FK vers Chauffeur

    // ── Constructeurs ──────────────────────────────────────────

    public Vehicule() {
    }

    public Vehicule(String marque, String modele, String immatriculation,
                    int placesDisponibles, int proprietaireId) {
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.placesDisponibles = placesDisponibles;
        this.proprietaireId = proprietaireId;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public int getPlacesDisponibles() {
        return placesDisponibles;
    }

    public void setPlacesDisponibles(int placesDisponibles) {
        this.placesDisponibles = placesDisponibles;
    }

    public int getProprietaireId() {
        return proprietaireId;
    }

    public void setProprietaireId(int proprietaireId) {
        this.proprietaireId = proprietaireId;
    }

    @Override
    public String toString() {
        return marque + " " + modele + " (" + immatriculation + ") - " + placesDisponibles + " places";
    }
}
