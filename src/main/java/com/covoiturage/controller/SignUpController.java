package com.covoiturage.controller;

import com.covoiturage.App;
import com.covoiturage.model.Chauffeur;
import com.covoiturage.model.Passager;
import com.covoiturage.model.User;
import com.covoiturage.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * Contrôleur pour la vue d'inscription.
 * Permet de créer un compte Passager ou Chauffeur.
 */
public class SignUpController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private RadioButton passagerRadio;
    @FXML private RadioButton chauffeurRadio;
    @FXML private ToggleGroup roleGroup;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        passagerRadio.setSelected(true);
    }

    @FXML
    private void handleSignUp() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (nom.isEmpty() || email.isEmpty() || telephone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!email.contains("@")) {
            showError("L'adresse email n'est pas valide.");
            return;
        }

        if (password.length() < 4) {
            showError("Le mot de passe doit contenir au moins 4 caractères.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            User user;
            if (chauffeurRadio.isSelected()) {
                user = new Chauffeur(nom, email, telephone, password);
            } else {
                user = new Passager(nom, email, telephone, password);
            }

            authService.creerCompte(user);

            // Show success and go back to login
            errorLabel.setText("✅ Compte créé avec succès ! Connectez-vous.");
            errorLabel.setStyle("-fx-text-fill: #4ecca3; -fx-font-size: 12px;");

            // Clear fields
            nomField.clear();
            emailField.clear();
            telephoneField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            showError("Erreur de chargement de la page.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px;");
    }
}
