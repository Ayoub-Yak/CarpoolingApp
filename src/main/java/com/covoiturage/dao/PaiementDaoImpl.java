package com.covoiturage.dao;

import com.covoiturage.db.DatabaseManager;
import com.covoiturage.model.Paiement;
import com.covoiturage.model.enums.StatutPaiement;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation DAO pour les paiements.
 */
public class PaiementDaoImpl implements PaiementDao {

    private final Connection connection;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PaiementDaoImpl() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void save(Paiement paiement) {
        String sql = "INSERT INTO paiements (montant, date_transaction, statut, reservation_id) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, paiement.getMontant());
            stmt.setString(2, paiement.getDateTransaction().format(FORMATTER));
            stmt.setString(3, paiement.getStatut().name());
            stmt.setInt(4, paiement.getReservationId());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                paiement.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("[PaiementDao] Erreur save : " + e.getMessage());
        }
    }

    @Override
    public Paiement findById(int id) {
        String sql = "SELECT * FROM paiements WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToPaiement(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PaiementDao] Erreur findById : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Paiement> findAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                paiements.add(mapRowToPaiement(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PaiementDao] Erreur findAll : " + e.getMessage());
        }
        return paiements;
    }

    @Override
    public Paiement findByReservationId(int reservationId) {
        String sql = "SELECT * FROM paiements WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToPaiement(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PaiementDao] Erreur findByReservationId : " + e.getMessage());
        }
        return null;
    }

    @Override
    public void update(Paiement paiement) {
        String sql = "UPDATE paiements SET montant = ?, date_transaction = ?, statut = ?, " +
                     "reservation_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, paiement.getMontant());
            stmt.setString(2, paiement.getDateTransaction().format(FORMATTER));
            stmt.setString(3, paiement.getStatut().name());
            stmt.setInt(4, paiement.getReservationId());
            stmt.setInt(5, paiement.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PaiementDao] Erreur update : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM paiements WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PaiementDao] Erreur delete : " + e.getMessage());
        }
    }

    // ── Mapping ────────────────────────────────────────────────

    private Paiement mapRowToPaiement(ResultSet rs) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setId(rs.getInt("id"));
        paiement.setMontant(rs.getDouble("montant"));
        paiement.setDateTransaction(LocalDateTime.parse(rs.getString("date_transaction"), FORMATTER));
        paiement.setStatut(StatutPaiement.valueOf(rs.getString("statut")));
        paiement.setReservationId(rs.getInt("reservation_id"));
        return paiement;
    }
}
