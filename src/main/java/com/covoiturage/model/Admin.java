package com.covoiturage.model;

/**
 * Représente un administrateur du système.
 * L'admin gère les utilisateurs (suspension, blocage, réactivation).
 */
public class Admin extends User {

    // ── Constructeurs ──────────────────────────────────────────

    public Admin() {
        super();
    }

    public Admin(String nom, String email, String telephone, String motDePasse) {
        super(nom, email, telephone, motDePasse);
    }

    // ── Type ───────────────────────────────────────────────────

    @Override
    public String getType() {
        return "ADMIN";
    }
}
