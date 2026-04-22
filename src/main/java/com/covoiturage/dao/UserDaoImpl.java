package com.covoiturage.dao;

import com.covoiturage.db.DatabaseManager;
import com.covoiturage.model.*;
import com.covoiturage.model.enums.StatutCompte;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation DAO pour les utilisateurs (User, Passager, Chauffeur, Admin).
 * Utilise la colonne 'type' pour distinguer les sous-classes.
 */
public class UserDaoImpl implements UserDao {

    private final Connection connection;

    public UserDaoImpl() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (nom, email, telephone, mot_de_passe, statut_compte, login_attempts, type, total_revenu) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getTelephone());
            stmt.setString(4, user.getMotDePasse());
            stmt.setString(5, user.getStatutCompte().name());
            stmt.setInt(6, user.getLoginAttempts());
            stmt.setString(7, user.getType());
            double totalRevenu = (user instanceof Chauffeur) ? ((Chauffeur) user).getTotalRevenu() : 0.0;
            stmt.setDouble(8, totalRevenu);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur save : " + e.getMessage());
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur findById : " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur findByEmail : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur findAll : " + e.getMessage());
        }
        return users;
    }

    @Override
    public List<User> findByType(String type) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur findByType : " + e.getMessage());
        }
        return users;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET nom = ?, email = ?, telephone = ?, mot_de_passe = ?, " +
                     "statut_compte = ?, login_attempts = ?, total_revenu = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getTelephone());
            stmt.setString(4, user.getMotDePasse());
            stmt.setString(5, user.getStatutCompte().name());
            stmt.setInt(6, user.getLoginAttempts());
            double totalRevenu = (user instanceof Chauffeur) ? ((Chauffeur) user).getTotalRevenu() : 0.0;
            stmt.setDouble(7, totalRevenu);
            stmt.setInt(8, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur update : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDao] Erreur delete : " + e.getMessage());
        }
    }

    // ── Mapping ────────────────────────────────────────────────

    /**
     * Mappe une ligne ResultSet vers le bon sous-type d'User
     * en fonction de la colonne 'type'.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        User user;

        switch (type) {
            case "CHAUFFEUR":
                user = new Chauffeur();
                break;
            case "ADMIN":
                user = new Admin();
                break;
            case "PASSAGER":
            default:
                user = new Passager();
                break;
        }

        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setEmail(rs.getString("email"));
        user.setTelephone(rs.getString("telephone"));
        user.setMotDePasseHashed(rs.getString("mot_de_passe"));
        user.setStatutCompte(StatutCompte.valueOf(rs.getString("statut_compte")));
        user.setLoginAttempts(rs.getInt("login_attempts"));

        if (user instanceof Chauffeur) {
            ((Chauffeur) user).setTotalRevenu(rs.getDouble("total_revenu"));
        }

        return user;
    }
}
