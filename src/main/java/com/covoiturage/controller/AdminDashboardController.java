package com.covoiturage.controller;

import com.covoiturage.App;
import com.covoiturage.SessionManager;
import com.covoiturage.model.*;
import com.covoiturage.model.enums.StatutCompte;
import com.covoiturage.service.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur du tableau de bord Administrateur.
 * Gère les utilisateurs (suspension, blocage, réactivation) et affiche les statistiques.
 */
public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label blockedUsersLabel;
    @FXML private Label totalTrajetsLabel;

    // ── Users Table ────────────────────────────────────────────
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colTelephone;
    @FXML private TableColumn<User, String> colType;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, Integer> colAttempts;
    @FXML private Label adminMessage;

    // ── Trajets Table ──────────────────────────────────────────
    @FXML private TableView<Trajet> allTrajetsTable;
    @FXML private TableColumn<Trajet, Integer> colTrajetId;
    @FXML private TableColumn<Trajet, String> colTrajetDepart;
    @FXML private TableColumn<Trajet, String> colTrajetArrivee;
    @FXML private TableColumn<Trajet, String> colTrajetDate;
    @FXML private TableColumn<Trajet, String> colTrajetPrix;
    @FXML private TableColumn<Trajet, String> colTrajetPlaces;
    @FXML private TableColumn<Trajet, String> colTrajetStatut;
    @FXML private TableColumn<Trajet, String> colTrajetChauffeur;

    // ── Notifications ──────────────────────────────────────────
    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, String> colNotifDate;
    @FXML private TableColumn<Notification, String> colNotifMessage;
    @FXML private TableColumn<Notification, String> colNotifStatut;

    private final AuthService authService = new AuthService();
    private final TrajetService trajetService = new TrajetService();
    private final NotificationService notificationService = new NotificationService();
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        Admin admin = (Admin) SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Bienvenue, " + admin.getNom() + " 👋");

        setupUsersTable();
        setupTrajetsTable();
        setupNotificationsTable();

        loadUsers();
        loadTrajets();
        loadNotifications();
    }

    // ── User Management ────────────────────────────────────────

    @FXML
    private void handleRefresh() {
        loadUsers();
        adminMessage.setText("Liste mise à jour.");
        adminMessage.setStyle("-fx-text-fill: #4ecca3;");
    }

    @FXML
    private void handleSuspendre() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminMessage.setText("⚠️ Sélectionnez un utilisateur.");
            adminMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }
        if (selected.getType().equals("ADMIN")) {
            adminMessage.setText("⚠️ Impossible de suspendre un admin.");
            adminMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        Admin admin = (Admin) SessionManager.getInstance().getCurrentUser();
        authService.suspendreCompte(admin, selected);
        adminMessage.setText("⏸️ Compte de " + selected.getNom() + " suspendu.");
        adminMessage.setStyle("-fx-text-fill: #f0a500;");
        loadUsers();
    }

    @FXML
    private void handleBloquer() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminMessage.setText("⚠️ Sélectionnez un utilisateur.");
            adminMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }
        if (selected.getType().equals("ADMIN")) {
            adminMessage.setText("⚠️ Impossible de bloquer un admin.");
            adminMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        authService.bloquerUtilisateur(selected);
        adminMessage.setText("🚫 Compte de " + selected.getNom() + " bloqué.");
        adminMessage.setStyle("-fx-text-fill: #e94560;");
        loadUsers();
    }

    @FXML
    private void handleReactiver() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminMessage.setText("⚠️ Sélectionnez un utilisateur.");
            adminMessage.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        authService.reactiverCompte(selected);
        adminMessage.setText("✅ Compte de " + selected.getNom() + " réactivé.");
        adminMessage.setStyle("-fx-text-fill: #4ecca3;");
        loadUsers();
    }

    @FXML
    private void handleRefreshTrajets() {
        loadTrajets();
    }

    // ── Table Setup ────────────────────────────────────────────

    private void setupUsersTable() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatutCompte().name()));
        colAttempts.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getLoginAttempts()).asObject());
    }

    private void setupTrajetsTable() {
        colTrajetId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colTrajetDepart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVilleDepart()));
        colTrajetArrivee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVilleArrivee()));
        colTrajetDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateHeureDepart().format(DT_FORMAT)));
        colTrajetPrix.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getPrixPlace())));
        colTrajetPlaces.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getNbPlacesDisponibles() + "/" + data.getValue().getNbPlacesTotal()));
        colTrajetStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut().name()));
        colTrajetChauffeur.setCellValueFactory(data -> {
            User u = authService.findById(data.getValue().getChauffeurId());
            return new SimpleStringProperty(u != null ? u.getNom() : "N/A");
        });
    }

    private void setupNotificationsTable() {
        colNotifDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateEnvoi().format(DT_FORMAT)));
        colNotifMessage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));
        colNotifStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isLu() ? "Lu" : "Non lu"));
    }

    // ── Data Loading ───────────────────────────────────────────

    private void loadUsers() {
        List<User> users = authService.findAll();
        // Exclude admins from the list
        users = users.stream()
                .filter(u -> !u.getType().equals("ADMIN"))
                .collect(Collectors.toList());

        usersTable.setItems(FXCollections.observableArrayList(users));

        // Update stats
        List<User> allUsers = authService.findAll();
        totalUsersLabel.setText(String.valueOf(allUsers.size() - 1)); // Exclude admin
        long blocked = allUsers.stream()
                .filter(u -> u.getStatutCompte() == StatutCompte.BLOQUE)
                .count();
        blockedUsersLabel.setText(String.valueOf(blocked));

        List<Trajet> allTrajets = trajetService.getAllTrajets();
        totalTrajetsLabel.setText(String.valueOf(allTrajets.size()));
    }

    private void loadTrajets() {
        List<Trajet> trajets = trajetService.getAllTrajets();
        allTrajetsTable.setItems(FXCollections.observableArrayList(trajets));
    }

    private void loadNotifications() {
        Admin admin = (Admin) SessionManager.getInstance().getCurrentUser();
        List<Notification> list = notificationService.getAllNotifications(admin.getId());
        notificationsTable.setItems(FXCollections.observableArrayList(list));
    }

    // ── Notifications Handlers ───────────────────────────────

    @FXML
    private void handleRefreshNotifications() {
        loadNotifications();
    }

    @FXML
    private void handleMarkAllAsRead() {
        Admin admin = (Admin) SessionManager.getInstance().getCurrentUser();
        List<Notification> unread = notificationService.getUnreadNotifications(admin.getId());
        for (Notification n : unread) {
            notificationService.marquerCommeLu(n.getId());
        }
        loadNotifications();
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
