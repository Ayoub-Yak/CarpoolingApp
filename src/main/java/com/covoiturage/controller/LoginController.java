package com.covoiturage.controller;

import com.covoiturage.App;
import com.covoiturage.SessionManager;
import com.covoiturage.model.User;
import com.covoiturage.service.AuthService;
import com.covoiturage.service.NotificationService;
import com.covoiturage.model.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Contrôleur pour la vue de connexion.
 * Gère l'authentification et la redirection vers le bon dashboard.
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            errorLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px;");
            return;
        }

        try {
            User user = authService.authentifierParEmail(email, password);
            if (user != null) {
                // Store in session
                SessionManager.getInstance().setCurrentUser(user);

                // Show unread notifications as popups
                java.util.List<Notification> unread = notificationService.getUnreadNotifications(user.getId());
                for (Notification n : unread) {
                    NotificationService.showPopup("Notification", n.getMessage());
                    notificationService.marquerCommeLu(n.getId());
                }

                // Redirect based on type
                switch (user.getType()) {
                    case "ADMIN":
                        App.setRoot("admin_dashboard");
                        break;
                    case "CHAUFFEUR":
                        App.setRoot("driver_dashboard");
                        break;
                    case "PASSAGER":
                    default:
                        App.setRoot("passenger_dashboard");
                        break;
                }
            } else {
                errorLabel.setText("Email ou mot de passe incorrect.");
                errorLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px;");
                
            }
        } catch (IllegalStateException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px;");
        } catch (IOException e) {
            errorLabel.setText("Erreur de chargement de la page.");
        }
    }

    @FXML
    private void handleGoToSignUp() {
        try {
            App.setRoot("signup");
        } catch (IOException e) {
            errorLabel.setText("Erreur de chargement de la page d'inscription.");
        }
    } 
}
