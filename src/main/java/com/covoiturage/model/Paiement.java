package com.covoiturage.model;

import com.covoiturage.model.enums.StatutPaiement;
import java.time.LocalDateTime;

/**
 * Représente un paiement lié à une réservation.
 * Suit le cycle : AUTORISE → CAPTURE → (REMBOURSE | ANNULE)
 */
public class Paiement {

    private int id;
    private double montant;
    private LocalDateTime dateTransaction;
    private StatutPaiement statut;
    private int reservationId; // FK vers Reservation

    // ── Constructeurs ──────────────────────────────────────────

    public Paiement() {
        this.dateTransaction = LocalDateTime.now();
        this.statut = StatutPaiement.AUTORISE;
    }

    public Paiement(double montant, int reservationId) {
        this.montant = montant;
        this.dateTransaction = LocalDateTime.now();
        this.statut = StatutPaiement.AUTORISE;
        this.reservationId = reservationId;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(LocalDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    @Override
    public String toString() {
        return "Paiement{id=" + id + ", montant=" + montant
                + ", statut=" + statut + ", reservationId=" + reservationId + "}";
    }
}
