package com.covoiturage.dao;

import com.covoiturage.db.DatabaseManager;
import com.covoiturage.model.Trajet;
import com.covoiturage.model.enums.StatutTrajet;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation DAO pour les trajets.
 */
public class TrajetDaoImpl implements TrajetDao {

    private final Connection connection;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TrajetDaoImpl() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void save(Trajet trajet) {
        String sql = "INSERT INTO trajets (ville_depart, ville_arrivee, date_heure_depart, " +
                     "prix_place, nb_places_total, nb_places_disponibles, statut, chauffeur_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, trajet.getVilleDepart());
            stmt.setString(2, trajet.getVilleArrivee());
            stmt.setString(3, trajet.getDateHeureDepart().format(FORMATTER));
            stmt.setDouble(4, trajet.getPrixPlace());
            stmt.setInt(5, trajet.getNbPlacesTotal());
            stmt.setInt(6, trajet.getNbPlacesDisponibles());
            stmt.setString(7, trajet.getStatut().name());
            stmt.setInt(8, trajet.getChauffeurId());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                trajet.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur save : " + e.getMessage());
        }
    }

    @Override
    public Trajet findById(int id) {
        String sql = "SELECT * FROM trajets WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToTrajet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur findById : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Trajet> findAll() {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajets";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trajets.add(mapRowToTrajet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur findAll : " + e.getMessage());
        }
        return trajets;
    }

    @Override
    public List<Trajet> findByChauffeurId(int chauffeurId) {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajets WHERE chauffeur_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, chauffeurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trajets.add(mapRowToTrajet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur findByChauffeurId : " + e.getMessage());
        }
        return trajets;
    }

    @Override
    public List<Trajet> findByVilleDepart(String villeDepart) {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajets WHERE LOWER(ville_depart) LIKE LOWER(?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + villeDepart + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trajets.add(mapRowToTrajet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur findByVilleDepart : " + e.getMessage());
        }
        return trajets;
    }

    @Override
    public List<Trajet> findDisponibles() {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajets WHERE statut = 'PREVU' AND nb_places_disponibles > 0";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trajets.add(mapRowToTrajet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur findDisponibles : " + e.getMessage());
        }
        return trajets;
    }

    @Override
    public void update(Trajet trajet) {
        String sql = "UPDATE trajets SET ville_depart = ?, ville_arrivee = ?, date_heure_depart = ?, " +
                     "prix_place = ?, nb_places_total = ?, nb_places_disponibles = ?, statut = ?, " +
                     "chauffeur_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, trajet.getVilleDepart());
            stmt.setString(2, trajet.getVilleArrivee());
            stmt.setString(3, trajet.getDateHeureDepart().format(FORMATTER));
            stmt.setDouble(4, trajet.getPrixPlace());
            stmt.setInt(5, trajet.getNbPlacesTotal());
            stmt.setInt(6, trajet.getNbPlacesDisponibles());
            stmt.setString(7, trajet.getStatut().name());
            stmt.setInt(8, trajet.getChauffeurId());
            stmt.setInt(9, trajet.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur update : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM trajets WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TrajetDao] Erreur delete : " + e.getMessage());
        }
    }

    // ── Mapping ────────────────────────────────────────────────

    private Trajet mapRowToTrajet(ResultSet rs) throws SQLException {
        Trajet trajet = new Trajet();
        trajet.setId(rs.getInt("id"));
        trajet.setVilleDepart(rs.getString("ville_depart"));
        trajet.setVilleArrivee(rs.getString("ville_arrivee"));
        trajet.setDateHeureDepart(LocalDateTime.parse(rs.getString("date_heure_depart"), FORMATTER));
        trajet.setPrixPlace(rs.getDouble("prix_place"));
        trajet.setNbPlacesTotal(rs.getInt("nb_places_total"));
        trajet.setNbPlacesDisponibles(rs.getInt("nb_places_disponibles"));
        trajet.setStatut(StatutTrajet.valueOf(rs.getString("statut")));
        trajet.setChauffeurId(rs.getInt("chauffeur_id"));
        return trajet;
    }
}
