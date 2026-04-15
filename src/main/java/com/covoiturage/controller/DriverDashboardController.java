package com.covoiturage.controller;

import com.covoiturage.App;
import com.covoiturage.SessionManager;
import com.covoiturage.model.*;
import com.covoiturage.model.enums.StatutReservation;
import com.covoiturage.model.enums.StatutTrajet;
import com.covoiturage.service.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur du tableau de bord Chauffeur.
 * Gère les trajets, véhicules, et réservations reçues.
 */
public class DriverDashboardController {

    // ── Top Bar ────────────────────────────────────────────────
    @FXML private Label welcomeLabel;
    @FXML private Label soldeLabel;

    // ── Proposer Trajet ────────────────────────────────────────
    @FXML private TextField departField;
    @FXML private TextField arriveeField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField prixField;
    @FXML private TextField placesField;
    @FXML private Label trajetMessage;

    // ── Mes Trajets ────────────────────────────────────────────
    @FXML private TableView<Trajet> mesTrajetsTable;
    @FXML private TableColumn<Trajet, String> colDepart;
    @FXML private TableColumn<Trajet, String> colArrivee;
    @FXML private TableColumn<Trajet, String> colDate;
    @FXML private TableColumn<Trajet, String> colPrix;
    @FXML private TableColumn<Trajet, String> colPlaces;
    @FXML private TableColumn<Trajet, String> colStatut;
    @FXML private Label mesTrajetMessage;

    // ── Réservations Reçues ────────────────────────────────────
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colResPassager;
    @FXML private TableColumn<Reservation, String> colResTrajet;
    @FXML private TableColumn<Reservation, String> colResDate;
    @FXML private TableColumn<Reservation, String> colResStatut;
    @FXML private Label resMessage;

    // ── Véhicules ──────────────────────────────────────────────
    @FXML private TextField marqueField;
    @FXML private TextField modeleField;
    @FXML private TextField immatField;
    @FXML private TextField placesVehField;
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, Integer> colVehId;
    @FXML private TableColumn<Vehicule, String> colMarque;
    @FXML private TableColumn<Vehicule, String> colModele;
    @FXML private TableColumn<Vehicule, String> colImmat;
    @FXML private TableColumn<Vehicule, Integer> colPlacesVeh;
    @FXML private Label vehiculeMessage;

    private final TrajetService trajetService = new TrajetService();
    private final ReservationService reservationService = new ReservationService();
    private final PaiementService paiementService = new PaiementService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final AuthService authService = new AuthService();

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Bienvenue, " + chauffeur.getNom() + " 👋");
        soldeLabel.setText("Solde: " + String.format("%.2f", chauffeur.getTotalRevenu()) + " DT");

        setupMesTrajetsTable();
        setupReservationsTable();
        setupVehiculesTable();

        loadMesTrajets();
        loadReservations();
        loadVehicules();
    }

    // ── Proposer un Trajet ─────────────────────────────────────

    @FXML
    private void handlePublierTrajet() {
        try {
            String depart = departField.getText().trim();
            String arrivee = arriveeField.getText().trim();
            LocalDate date = datePicker.getValue();
            String heure = heureField.getText().trim();
            double prix = Double.parseDouble(prixField.getText().trim());
            int places = Integer.parseInt(placesField.getText().trim());

            if (depart.isEmpty() || arrivee.isEmpty() || date == null || heure.isEmpty()) {
                trajetMessage.setText("⚠️ Veuillez remplir tous les champs.");
                trajetMessage.setStyle("-fx-text-fill: #e94560;");
                return;
            }

            String[] parts = heure.split(":");
            LocalTime time = LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();
            Trajet trajet = new Trajet(depart, arrivee, dateTime, prix, places, chauffeur.getId());
            trajetService.proposerTrajet(chauffeur, trajet);

            trajetMessage.setText("✅ Trajet publié avec succès ! (ID: " + trajet.getId() + ")");
            trajetMessage.setStyle("-fx-text-fill: #4ecca3;");

            // Clear fields
            departField.clear();
            arriveeField.clear();
            datePicker.setValue(null);
            heureField.clear();
            prixField.clear();
            placesField.clear();

            loadMesTrajets();
        } catch (NumberFormatException e) {
            trajetMessage.setText("⚠️ Prix et nombre de places doivent être des nombres valides.");
            trajetMessage.setStyle("-fx-text-fill: #e94560;");
        } catch (Exception e) {
            trajetMessage.setText("❌ Erreur : " + e.getMessage());
            trajetMessage.setStyle("-fx-text-fill: #e94560;");
        }
    }

    // ── Mes Trajets ────────────────────────────────────────────

    @FXML
    private void handleRefreshTrajets() {
        loadMesTrajets();
        mesTrajetMessage.setText("Liste mise à jour.");
    }

    @FXML
    private void handleAnnulerTrajet() {
        Trajet selected = mesTrajetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mesTrajetMessage.setText("⚠️ Sélectionnez un trajet.");
            mesTrajetMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (selected.getStatut() == StatutTrajet.ANNULE) {
            mesTrajetMessage.setText("Ce trajet est déjà annulé.");
            return;
        }

        double penalite = reservationService.annulerReservationsParChauffeur(selected);
        Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();

        if (penalite > 0) {
            mesTrajetMessage.setText("Trajet annulé. Pénalité : " + String.format("%.2f", penalite) + " DT");
            mesTrajetMessage.setStyle("-fx-text-fill: #e94560;");
        } else {
            mesTrajetMessage.setText("✅ Trajet annulé sans pénalité.");
            mesTrajetMessage.setStyle("-fx-text-fill: #4ecca3;");
        }

        loadMesTrajets();
        loadReservations();
    }

    // ── Réservations Reçues ────────────────────────────────────

    @FXML
    private void handleRefreshReservations() {
        loadReservations();
        resMessage.setText("Liste mise à jour.");
    }

    @FXML
    private void handleAccepterReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resMessage.setText("⚠️ Sélectionnez une réservation.");
            resMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (selected.getStatut() != StatutReservation.EN_ATTENTE) {
            resMessage.setText("Seules les réservations en attente peuvent être acceptées.");
            return;
        }

        reservationService.confirmerReservation(selected);

        // Capture payment
        Paiement paiement = paiementService.findByReservationId(selected.getId());
        if (paiement != null) {
            paiementService.capturerPaiement(paiement);
        }

        resMessage.setText("✅ Réservation acceptée et paiement capturé.");
        resMessage.setStyle("-fx-text-fill: #4ecca3;");
        loadReservations();
    }

    @FXML
    private void handleRefuserReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resMessage.setText("⚠️ Sélectionnez une réservation.");
            resMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        selected.setStatut(StatutReservation.REFUSEE);
        reservationService.update(selected);

        // Refund payment
        Paiement paiement = paiementService.findByReservationId(selected.getId());
        if (paiement != null) {
            paiementService.annulerPaiement(paiement);
        }

        // Free the seat
        Trajet trajet = trajetService.findById(selected.getTrajetId());
        if (trajet != null) {
            trajet.retirerPassager(selected);
            trajetService.update(trajet);
        }

        resMessage.setText("❌ Réservation refusée.");
        resMessage.setStyle("-fx-text-fill: #e94560;");
        loadReservations();
    }

    // ── Véhicules ──────────────────────────────────────────────

    @FXML
    private void handleAjouterVehicule() {
        try {
            String marque = marqueField.getText().trim();
            String modele = modeleField.getText().trim();
            String immat = immatField.getText().trim();
            int places = Integer.parseInt(placesVehField.getText().trim());

            if (marque.isEmpty() || modele.isEmpty() || immat.isEmpty()) {
                vehiculeMessage.setText("⚠️ Remplissez tous les champs.");
                vehiculeMessage.setStyle("-fx-text-fill: #e94560;");
                return;
            }

            Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();
            Vehicule vehicule = new Vehicule(marque, modele, immat, places, chauffeur.getId());
            vehiculeService.ajouterVehicule(chauffeur, vehicule);

            vehiculeMessage.setText("✅ Véhicule ajouté !");
            vehiculeMessage.setStyle("-fx-text-fill: #4ecca3;");

            marqueField.clear();
            modeleField.clear();
            immatField.clear();
            placesVehField.clear();

            loadVehicules();
        } catch (NumberFormatException e) {
            vehiculeMessage.setText("⚠️ Le nombre de places doit être un nombre.");
            vehiculeMessage.setStyle("-fx-text-fill: #e94560;");
        }
    }

    @FXML
    private void handleSupprimerVehicule() {
        Vehicule selected = vehiculesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            vehiculeMessage.setText("⚠️ Sélectionnez un véhicule.");
            vehiculeMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }
        vehiculeService.delete(selected.getId());
        vehiculeMessage.setText("🗑️ Véhicule supprimé.");
        vehiculeMessage.setStyle("-fx-text-fill: #e94560;");
        loadVehicules();
    }

    // ── Setup Tables ───────────────────────────────────────────

    private void setupMesTrajetsTable() {
        colDepart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVilleDepart()));
        colArrivee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVilleArrivee()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateHeureDepart().format(DT_FORMAT)));
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getPrixPlace())));
        colPlaces.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getNbPlacesDisponibles() + "/" + data.getValue().getNbPlacesTotal()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut().name()));
    }

    private void setupReservationsTable() {
        colResId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colResPassager.setCellValueFactory(data -> {
            User u = authService.findById(data.getValue().getPassagerId());
            return new SimpleStringProperty(u != null ? u.getNom() : "N/A");
        });
        colResTrajet.setCellValueFactory(data -> {
            Trajet t = trajetService.findById(data.getValue().getTrajetId());
            if (t != null) {
                return new SimpleStringProperty(t.getVilleDepart() + " → " + t.getVilleArrivee());
            }
            return new SimpleStringProperty("N/A");
        });
        colResDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateReservation().format(DT_FORMAT)));
        colResStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut().name()));
    }

    private void setupVehiculesTable() {
        colVehId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colMarque.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarque()));
        colModele.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModele()));
        colImmat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImmatriculation()));
        colPlacesVeh.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPlacesDisponibles()).asObject());
    }

    // ── Data Loading ───────────────────────────────────────────

    private void loadMesTrajets() {
        Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();
        List<Trajet> trajets = trajetService.getTrajetsByChauffeur(chauffeur.getId());
        mesTrajetsTable.setItems(FXCollections.observableArrayList(trajets));
    }

    private void loadReservations() {
        Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();
        List<Trajet> mesTrajets = trajetService.getTrajetsByChauffeur(chauffeur.getId());
        List<Reservation> allReservations = new ArrayList<>();
        for (Trajet t : mesTrajets) {
            allReservations.addAll(reservationService.getReservationsByTrajet(t.getId()));
        }
        reservationsTable.setItems(FXCollections.observableArrayList(allReservations));
    }

    private void loadVehicules() {
        Chauffeur chauffeur = (Chauffeur) SessionManager.getInstance().getCurrentUser();
        List<Vehicule> vehicules = vehiculeService.getVehiculesByChauffeur(chauffeur.getId());
        vehiculesTable.setItems(FXCollections.observableArrayList(vehicules));
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
