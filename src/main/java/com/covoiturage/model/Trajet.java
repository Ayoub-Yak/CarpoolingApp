package com.covoiturage.model;

import com.covoiturage.model.enums.StatutTrajet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un trajet proposé par un chauffeur.
 * Gère la logique d'ajout de passagers et le passage automatique
 * au statut COMPLET lorsque toutes les places sont prises.
 */
public class Trajet {

    private int id;
    private String villeDepart;
    private String villeArrivee;
    private LocalDateTime dateHeureDepart;
    private double prixPlace;
    private int nbPlacesTotal;
    private int nbPlacesDisponibles;
    private StatutTrajet statut;
    private int chauffeurId; // FK vers Chauffeur
    private List<Reservation> reservations;

    // ── Constructeurs ──────────────────────────────────────────

    public Trajet() {
        this.statut = StatutTrajet.PREVU;
        this.reservations = new ArrayList<>();
    }

    public Trajet(String villeDepart, String villeArrivee, LocalDateTime dateHeureDepart,
                  double prixPlace, int nbPlacesTotal, int chauffeurId) {
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.dateHeureDepart = dateHeureDepart;
        this.prixPlace = prixPlace;
        this.nbPlacesTotal = nbPlacesTotal;
        this.nbPlacesDisponibles = nbPlacesTotal;
        this.statut = StatutTrajet.PREVU;
        this.chauffeurId = chauffeurId;
        this.reservations = new ArrayList<>();
    }

    // ── Logique métier ─────────────────────────────────────────

    /**
     * Ajoute un passager au trajet via une réservation.
     * Décrémente nbPlacesDisponibles.
     * Si la dernière place est prise → statut = COMPLET.
     *
     * @param reservation la réservation à ajouter
     * @throws IllegalStateException si aucune place n'est disponible
     */
    public void ajouterPassager(Reservation reservation) {
        if (nbPlacesDisponibles <= 0) {
            throw new IllegalStateException("Aucune place disponible pour ce trajet.");
        }
        this.reservations.add(reservation);
        this.nbPlacesDisponibles--;
        if (this.nbPlacesDisponibles == 0) {
            this.statut = StatutTrajet.COMPLET;
        }
    }

    /**
     * Retire un passager du trajet (lors d'une annulation).
     * Incrémente nbPlacesDisponibles et repasse en PREVU si nécessaire.
     */
    public void retirerPassager(Reservation reservation) {
        if (this.reservations.remove(reservation)) {
            this.nbPlacesDisponibles++;
            if (this.statut == StatutTrajet.COMPLET) {
                this.statut = StatutTrajet.PREVU;
            }
        }
    }

    // ── Getters & Setters ──────────────────────────────────────

    /**
     * Retourne une copie défensive de la liste des réservations.
     */
    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVilleDepart() {
        return villeDepart;
    }

    public void setVilleDepart(String villeDepart) {
        this.villeDepart = villeDepart;
    }

    public String getVilleArrivee() {
        return villeArrivee;
    }

    public void setVilleArrivee(String villeArrivee) {
        this.villeArrivee = villeArrivee;
    }

    public LocalDateTime getDateHeureDepart() {
        return dateHeureDepart;
    }

    public void setDateHeureDepart(LocalDateTime dateHeureDepart) {
        this.dateHeureDepart = dateHeureDepart;
    }

    public double getPrixPlace() {
        return prixPlace;
    }

    public void setPrixPlace(double prixPlace) {
        this.prixPlace = prixPlace;
    }

    public int getNbPlacesTotal() {
        return nbPlacesTotal;
    }

    public void setNbPlacesTotal(int nbPlacesTotal) {
        this.nbPlacesTotal = nbPlacesTotal;
    }

    public int getNbPlacesDisponibles() {
        return nbPlacesDisponibles;
    }

    public void setNbPlacesDisponibles(int nbPlacesDisponibles) {
        this.nbPlacesDisponibles = nbPlacesDisponibles;
    }

    public StatutTrajet getStatut() {
        return statut;
    }

    public void setStatut(StatutTrajet statut) {
        this.statut = statut;
    }

    public int getChauffeurId() {
        return chauffeurId;
    }

    public void setChauffeurId(int chauffeurId) {
        this.chauffeurId = chauffeurId;
    }

    @Override
    public String toString() {
        return villeDepart + " → " + villeArrivee + " | " + dateHeureDepart
                + " | " + prixPlace + " DT | " + nbPlacesDisponibles + "/" + nbPlacesTotal + " places";
    }
}
