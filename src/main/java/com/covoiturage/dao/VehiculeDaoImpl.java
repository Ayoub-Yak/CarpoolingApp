package com.covoiturage.dao;

import com.covoiturage.db.DatabaseManager;
import com.covoiturage.model.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation DAO pour les véhicules.
 */
public class VehiculeDaoImpl implements VehiculeDao {

    private final Connection connection;

    public VehiculeDaoImpl() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void save(Vehicule vehicule) {
        String sql = "INSERT INTO vehicules (marque, modele, immatriculation, places_disponibles, proprietaire_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehicule.getMarque());
            stmt.setString(2, vehicule.getModele());
            stmt.setString(3, vehicule.getImmatriculation());
            stmt.setInt(4, vehicule.getPlacesDisponibles());
            stmt.setInt(5, vehicule.getProprietaireId());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                vehicule.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("[VehiculeDao] Erreur save : " + e.getMessage());
        }
    }

    @Override
    public Vehicule findById(int id) {
        String sql = "SELECT * FROM vehicules WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToVehicule(rs);
            }
        } catch (SQLException e) {
            System.err.println("[VehiculeDao] Erreur findById : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Vehicule> findAll() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vehicules.add(mapRowToVehicule(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VehiculeDao] Erreur findAll : " + e.getMessage());
        }
        return vehicules;
    }

    @Override
    public List<Vehicule> findByProprietaireId(int proprietaireId) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules WHERE proprietaire_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, proprietaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vehicules.add(mapRowToVehicule(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VehiculeDao] Erreur findByProprietaireId : " + e.getMessage());
        }
        return vehicules;
    }

    @Override
    public void update(Vehicule vehicule) {
        String sql = "UPDATE vehicules SET marque = ?, modele = ?, immatriculation = ?, " +
                     "places_disponibles = ?, proprietaire_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vehicule.getMarque());
            stmt.setString(2, vehicule.getModele());
            stmt.setString(3, vehicule.getImmatriculation());
            stmt.setInt(4, vehicule.getPlacesDisponibles());
            stmt.setInt(5, vehicule.getProprietaireId());
            stmt.setInt(6, vehicule.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VehiculeDao] Erreur update : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM vehicules WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VehiculeDao] Erreur delete : " + e.getMessage());
        }
    }

    // ── Mapping ────────────────────────────────────────────────

    private Vehicule mapRowToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getInt("id"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        vehicule.setPlacesDisponibles(rs.getInt("places_disponibles"));
        vehicule.setProprietaireId(rs.getInt("proprietaire_id"));
        return vehicule;
    }
}
