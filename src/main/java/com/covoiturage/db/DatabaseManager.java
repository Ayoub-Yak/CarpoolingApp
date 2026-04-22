package com.covoiturage.db;

import com.covoiturage.model.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestionnaire de base de données SQLite (Singleton).
 * Gère la connexion et l'initialisation des tables.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:covoiturage.db";
    private static DatabaseManager instance;
    private Connection connection;

    // ── Singleton ──────────────────────────────────────────────

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("[DB] Connexion à covoiturage.db établie.");
            initializeTables();
            seedDefaultAdmin();
        } catch (SQLException e) {
            System.err.println("[DB] Erreur de connexion : " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter à la base de données.", e);
        }
    }

    /**
     * Retourne l'instance unique du DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Retourne la connexion active à la base de données.
     */
    public Connection getConnection() {
        return connection;
    }

    // ── Initialisation des tables ─────────────────────────────   ─

    /**
     * Crée toutes les tables si elles n'existent pas encore.
     */
    private void initializeTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Activer les clés étrangères
        stmt.execute("PRAGMA foreign_keys = ON;");

        // Table users
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    nom TEXT NOT NULL," +
            "    email TEXT UNIQUE NOT NULL," +
            "    telephone TEXT," +
            "    mot_de_passe TEXT NOT NULL," +
            "    statut_compte TEXT DEFAULT 'ACTIF'," +
            "    login_attempts INTEGER DEFAULT 0," +
            "    total_revenu REAL NOT NULL DEFAULT 0.0," +
            "    type TEXT NOT NULL" +  // PASSAGER, CHAUFFEUR, ADMIN
            ")"
        );

        // Table vehicules
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS vehicules (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    marque TEXT NOT NULL," +
            "    modele TEXT NOT NULL," +
            "    immatriculation TEXT UNIQUE NOT NULL," +
            "    places_disponibles INTEGER NOT NULL," +
            "    proprietaire_id INTEGER NOT NULL," +
            "    FOREIGN KEY (proprietaire_id) REFERENCES users(id)" +
            ")"
        );

        // Table trajets
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS trajets (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    ville_depart TEXT NOT NULL," +
            "    ville_arrivee TEXT NOT NULL," +
            "    date_heure_depart TEXT NOT NULL," +
            "    prix_place REAL NOT NULL," +
            "    nb_places_total INTEGER NOT NULL," +
            "    nb_places_disponibles INTEGER NOT NULL," +
            "    statut TEXT DEFAULT 'PREVU'," +
            "    chauffeur_id INTEGER NOT NULL," +
            "    FOREIGN KEY (chauffeur_id) REFERENCES users(id)" +
            ")"
        );

        // Table reservations
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS reservations (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    date_reservation TEXT NOT NULL," +
            "    statut TEXT DEFAULT 'EN_ATTENTE'," +
            "    passager_id INTEGER NOT NULL," +
            "    trajet_id INTEGER NOT NULL," +
            "    FOREIGN KEY (passager_id) REFERENCES users(id)," +
            "    FOREIGN KEY (trajet_id) REFERENCES trajets(id)" +
            ")"
        );

        // Table paiements
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS paiements (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    montant REAL NOT NULL," +
            "    date_transaction TEXT NOT NULL," +
            "    statut TEXT DEFAULT 'AUTORISE'," +
            "    reservation_id INTEGER NOT NULL," +
            "    FOREIGN KEY (reservation_id) REFERENCES reservations(id)" +
            ")"
        );

        // Table notifications
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS notifications (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    user_id INTEGER NOT NULL," +
            "    message TEXT NOT NULL," +
            "    date_envoi TEXT NOT NULL," +
            "    lu INTEGER DEFAULT 0," +  // 0 = non lu, 1 = lu
            "    FOREIGN KEY (user_id) REFERENCES users(id)" +
            ")"
        );

        stmt.close();

        ensureUsersTotalRevenuColumn();
        System.out.println("[DB] Tables initialisées avec succès.");
    }

    /**
     * Migration légère : ajoute users.total_revenu si la colonne manque
     * (cas d'une base déjà existante).
     */
    private void ensureUsersTotalRevenuColumn() {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)")) {
            boolean hasTotalRevenu = false;
            while (rs.next()) {
                if ("total_revenu".equalsIgnoreCase(rs.getString("name"))) {
                    hasTotalRevenu = true;
                    break;
                }
            }

            if (!hasTotalRevenu) {
                stmt.execute("ALTER TABLE users ADD COLUMN total_revenu REAL NOT NULL DEFAULT 0.0");
                System.out.println("[DB] Migration appliquée : colonne users.total_revenu ajoutée.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erreur migration users.total_revenu : " + e.getMessage());
        }
    }

    // ── Seed Admin par défaut ──────────────────────────────────

    /**
     * Crée un compte admin par défaut s'il n'existe pas déjà.
     * Email: admin@covoiturage.com / Mot de passe: admin123
     */
    private void seedDefaultAdmin() {
        try {
            var checkStmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE type = 'ADMIN'"
            );
            var rs = checkStmt.executeQuery();
             if (rs.next() && rs.getInt(1) == 0) {
                var insertStmt = connection.prepareStatement(
                    "INSERT INTO users (nom, email, telephone, mot_de_passe, statut_compte, login_attempts, type) " +
                    "VALUES (?, ?, ?, ?, 'ACTIF', 0, 'ADMIN')"
                );
                insertStmt.setString(1, "Administrateur");
                insertStmt.setString(2, "admin@covoiturage.com");
                insertStmt.setString(3, "0000000000");
                insertStmt.setString(4, User.hashPassword("admin123"));
                insertStmt.executeUpdate();
                insertStmt.close();
                System.out.println("[DB] Compte admin par défaut créé (admin@covoiturage.com / admin123).");
            }
            checkStmt.close();
        } catch (SQLException e) {
            System.err.println("[DB] Erreur lors du seed admin : " + e.getMessage());
        }
    }

    // ── Fermeture ──────────────────────────────────────────────

    /**
     * Ferme la connexion à la base de données.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connexion fermée.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erreur lors de la fermeture : " + e.getMessage());
        }
    }
}
