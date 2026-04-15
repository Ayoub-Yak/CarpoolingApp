package com.covoiturage.controller;

import com.covoiturage.App;
import com.covoiturage.SessionManager;
import com.covoiturage.model.*;
import com.covoiturage.model.enums.StatutReservation;
import com.covoiturage.service.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur du tableau de bord Passager.
 * Recherche de trajets, réservation, et gestion des réservations.
 */
public class PassengerDashboardController {

    // ── Recherche Trajets ──────────────────────────────────────
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Trajet> trajetsTable;
    @FXML private TableColumn<Trajet, String> colDepart;
    @FXML private TableColumn<Trajet, String> colArrivee;
    @FXML private TableColumn<Trajet, String> colDate;
    @FXML private TableColumn<Trajet, String> colPrix;
    @FXML private TableColumn<Trajet, String> colPlaces;
    @FXML private TableColumn<Trajet, String> colStatut;
    @FXML private Label trajetMessage;

    // ── Mes Réservations ───────────────────────────────────────
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colResTrajet;
    @FXML private TableColumn<Reservation, String> colResDate;
    @FXML private TableColumn<Reservation, String> colResStatut;
    @FXML private Label reservationMessage;

    private final TrajetService trajetService = new TrajetService();
    private final ReservationService reservationService = new ReservationService();
    private final PaiementService paiementService = new PaiementService();
    private final AuthService authService = new AuthService();

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        Passager passager = (Passager) SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Bienvenue, " + passager.getNom() + " 👋");

        // Setup trajets table
        colDepart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVilleDepart()));
        colArrivee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVilleArrivee()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateHeureDepart().format(DT_FORMAT)));
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getPrixPlace())));
        colPlaces.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getNbPlacesDisponibles() + "/" + data.getValue().getNbPlacesTotal()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut().name()));

        // Setup reservations table
        colResId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colResTrajet.setCellValueFactory(data -> {
            Trajet t = trajetService.findById(data.getValue().getTrajetId());
            if (t != null) {
                return new SimpleStringProperty(t.getVilleDepart() + " → " + t.getVilleArrivee());
            }
            return new SimpleStringProperty("N/A");
        });
        colResDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateReservation().format(DT_FORMAT)));
        colResStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut().name()));

        // Load data
        handleShowAll();
        loadReservations();
    }

    @FXML
    private void handleSearch() {
        String ville = searchField.getText().trim();
        if (ville.isEmpty()) {
            handleShowAll();
            return;
        }
        List<Trajet> trajets = trajetService.getTrajetsDisponibles();
        trajets.removeIf(t ->
            !t.getVilleDepart().toLowerCase().contains(ville.toLowerCase()) &&
            !t.getVilleArrivee().toLowerCase().contains(ville.toLowerCase())
        );
        trajetsTable.setItems(FXCollections.observableArrayList(trajets));
        trajetMessage.setText(trajets.size() + " trajet(s) trouvé(s).");
    }

    @FXML
    private void handleShowAll() {
        List<Trajet> trajets = trajetService.getTrajetsDisponibles();
        trajetsTable.setItems(FXCollections.observableArrayList(trajets));
        trajetMessage.setText(trajets.size() + " trajet(s) disponible(s).");
    }

    @FXML
    private void handleReserver() {
        Trajet selected = trajetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            trajetMessage.setText("⚠️ Veuillez sélectionner un trajet.");
            trajetMessage.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px;");
            return;
        }

        try {
            Passager passager = (Passager) SessionManager.getInstance().getCurrentUser();
            Reservation reservation = reservationService.creerReservation(passager, selected);

            // Create payment authorization
            paiementService.payer(reservation, selected.getPrixPlace());

            trajetMessage.setText("✅ Réservation effectuée ! (ID: " + reservation.getId() + ")");
            trajetMessage.setStyle("-fx-text-fill: #4ecca3; -fx-font-size: 13px;");

            handleShowAll();
            loadReservations();
        } catch (IllegalStateException e) {
            trajetMessage.setText("❌ " + e.getMessage());
            trajetMessage.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px;");
        }
    }

    @FXML
    private void handleRefreshReservations() {
        loadReservations();
        reservationMessage.setText("Liste mise à jour.");
    }

    @FXML
    private void handleAnnulerReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            reservationMessage.setText("⚠️ Veuillez sélectionner une réservation.");
            reservationMessage.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px;");
            return;
        }

        if (selected.getStatut() == StatutReservation.ANNULEE) {
            reservationMessage.setText("Cette réservation est déjà annulée.");
            return;
        }

        Passager passager = (Passager) SessionManager.getInstance().getCurrentUser();
        int refundPercent = reservationService.annulerReservationParPassager(passager, selected);

        // Handle refund
        Paiement paiement = paiementService.findByReservationId(selected.getId());
        if (paiement != null) {
            double refundAmount = paiement.getMontant() * refundPercent / 100.0;
            paiementService.rembourser(paiement, refundAmount);
        }

        reservationMessage.setText("✅ Réservation annulée. Remboursement : " + refundPercent + "%");
        reservationMessage.setStyle("-fx-text-fill: #4ecca3; -fx-font-size: 13px;");
        loadReservations();
        handleShowAll();
    }

    private void loadReservations() {
        Passager passager = (Passager) SessionManager.getInstance().getCurrentUser();
        List<Reservation> reservations = reservationService.getReservationsByPassager(passager.getId());
        reservationsTable.setItems(FXCollections.observableArrayList(reservations));
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
