package com.covoiturage.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un passager dans le système de covoiturage.
 * Un passager peut effectuer des réservations sur des trajets.
 */
public class Passager extends User {

    private List<Reservation> reservations;

    // ── Constructeurs ──────────────────────────────────────────

    public Passager() {
        super();
        this.reservations = new ArrayList<>();
    }

    public Passager(String nom, String email, String telephone, String motDePasse) {
        super(nom, email, telephone, motDePasse);
        this.reservations = new ArrayList<>();
    }

    // ── Gestion des réservations ───────────────────────────────

    /**
     * Retourne une copie défensive de la liste des réservations.
     * Conformément aux règles du PDF : les getters de collections
     * doivent retourner une copie immuable.
     */
    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    /**
     * Ajoute une réservation à la liste interne.
     */
    public void ajouterReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    /**
     * Remplace la liste interne des réservations.
     * Utilisé lors du chargement depuis la base de données.
     */
    public void setReservations(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
    }

    // ── Type ───────────────────────────────────────────────────

    @Override
    public String getType() {
        return "PASSAGER";
    }
}
