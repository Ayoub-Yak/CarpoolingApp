package com.covoiturage.dao;

import com.covoiturage.db.DatabaseManager;
import com.covoiturage.model.Reservation;
import com.covoiturage.model.enums.StatutReservation;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation DAO pour les réservations.
 */
public class ReservationDaoImpl implements ReservationDao {

    private final Connection connection;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ReservationDaoImpl() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void save(Reservation reservation) {
        String sql = "INSERT INTO reservations (date_reservation, statut, passager_id, trajet_id) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reservation.getDateReservation().format(FORMATTER));
            stmt.setString(2, reservation.getStatut().name());
            stmt.setInt(3, reservation.getPassagerId());
            stmt.setInt(4, reservation.getTrajetId());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                reservation.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur save : " + e.getMessage());
        }
    }

    @Override
    public Reservation findById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToReservation(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur findById : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reservations.add(mapRowToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur findAll : " + e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<Reservation> findByPassagerId(int passagerId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE passager_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, passagerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapRowToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur findByPassagerId : " + e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<Reservation> findByTrajetId(int trajetId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE trajet_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trajetId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapRowToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur findByTrajetId : " + e.getMessage());
        }
        return reservations;
    }

    @Override
    public void update(Reservation reservation) {
        String sql = "UPDATE reservations SET date_reservation = ?, statut = ?, " +
                     "passager_id = ?, trajet_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reservation.getDateReservation().format(FORMATTER));
            stmt.setString(2, reservation.getStatut().name());
            stmt.setInt(3, reservation.getPassagerId());
            stmt.setInt(4, reservation.getTrajetId());
            stmt.setInt(5, reservation.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur update : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ReservationDao] Erreur delete : " + e.getMessage());
        }
    }

    // ── Mapping ────────────────────────────────────────────────

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setDateReservation(LocalDateTime.parse(rs.getString("date_reservation"), FORMATTER));
        reservation.setStatut(StatutReservation.valueOf(rs.getString("statut")));
        reservation.setPassagerId(rs.getInt("passager_id"));
        reservation.setTrajetId(rs.getInt("trajet_id"));
        return reservation;
    }
}
