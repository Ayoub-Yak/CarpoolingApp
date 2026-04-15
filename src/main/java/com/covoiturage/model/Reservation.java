package com.covoiturage.model;

import com.covoiturage.model.enums.StatutReservation;
import java.time.LocalDateTime;

/**
 * Représente une réservation faite par un passager sur un trajet.
 */
public class Reservation {

    private int id;
    private LocalDateTime dateReservation;
    private StatutReservation statut;
    private int passagerId;  // FK vers Passager
    private int trajetId;    // FK vers Trajet

    // ── Constructeurs ──────────────────────────────────────────

    public Reservation() {
        this.dateReservation = LocalDateTime.now();
        this.statut = StatutReservation.EN_ATTENTE;
    }

    public Reservation(int passagerId, int trajetId) {
        this.dateReservation = LocalDateTime.now();
        this.statut = StatutReservation.EN_ATTENTE;
        this.passagerId = passagerId;
        this.trajetId = trajetId;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public int getPassagerId() {
        return passagerId;
    }

    public void setPassagerId(int passagerId) {
        this.passagerId = passagerId;
    }

    public int getTrajetId() {
        return trajetId;
    }

    public void setTrajetId(int trajetId) {
        this.trajetId = trajetId;
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", statut=" + statut
                + ", passagerId=" + passagerId + ", trajetId=" + trajetId + "}";
    }
}
