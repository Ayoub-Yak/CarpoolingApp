package com.covoiturage.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un chauffeur dans le système de covoiturage.
 * Un chauffeur possède des véhicules et propose des trajets.
 */
public class Chauffeur extends User {

    private List<Vehicule> vehicules;
    private List<Trajet> trajetsPropose;
    private double totalRevenu;

    // ── Constructeurs ──────────────────────────────────────────

    public Chauffeur() {
        super();
        this.vehicules = new ArrayList<>();
        this.trajetsPropose = new ArrayList<>();
        this.totalRevenu = 0.0;
    }

    public Chauffeur(String nom, String email, String telephone, String motDePasse) {
        super(nom, email, telephone, motDePasse);
        this.vehicules = new ArrayList<>();
        this.trajetsPropose = new ArrayList<>();
        this.totalRevenu = 0.0;
    }

    // ── Gestion des véhicules ──────────────────────────────────

    /**
     * Retourne une copie défensive de la liste des véhicules.
     */
    public List<Vehicule> getVehicules() {
        return List.copyOf(vehicules);
    }

    /**
     * Ajoute un véhicule au chauffeur.
     */
    public void ajouterVehicule(Vehicule vehicule) {
        this.vehicules.add(vehicule);
    }

    public void setVehicules(List<Vehicule> vehicules) {
        this.vehicules = new ArrayList<>(vehicules);
    }

    // ── Gestion des trajets ────────────────────────────────────

    /**
     * Retourne une copie défensive de la liste des trajets proposés.
     */
    public List<Trajet> getTrajetsPropose() {
        return List.copyOf(trajetsPropose);
    }

    /**
     * Alias pour getTrajetsPropose() — conforme au diagramme UML.
     */
    public List<Trajet> getTrajetsProposes() {
        return getTrajetsPropose();
    }

    /**
     * Ajoute un trajet proposé par le chauffeur.
     */
    public void ajouterTrajet(Trajet trajet) {
        this.trajetsPropose.add(trajet);
    }

    public void setTrajetsPropose(List<Trajet> trajetsPropose) {
        this.trajetsPropose = new ArrayList<>(trajetsPropose);
    }

    // ── Revenus ────────────────────────────────────────────────

    /**
     * Consulter le solde total des revenus du chauffeur.
     */
    public double consulterSolde() {
        return totalRevenu;
    }

    public double getTotalRevenu() {
        return totalRevenu;
    }

    public void setTotalRevenu(double totalRevenu) {
        this.totalRevenu = totalRevenu;
    }

    /**
     * Ajoute un montant aux revenus du chauffeur.
     */
    public void ajouterRevenu(double montant) {
        this.totalRevenu += montant;
    }

    // ── Type ───────────────────────────────────────────────────

    @Override
    public String getType() {
        return "CHAUFFEUR";
    }
}
