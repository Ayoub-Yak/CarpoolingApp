package com.covoiturage.model;

import com.covoiturage.model.enums.StatutCompte;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe abstraite représentant un utilisateur du système.
 * Toutes les sous-classes (Passager, Chauffeur, Admin) en héritent.
 */
public abstract class User {

    private int id;
    private String nom;
    private String email;
    private String telephone;
    private String motDePasse; // Stocké en SHA-256
    private StatutCompte statutCompte;
    private int loginAttempts;

    // ── Constructeurs ──────────────────────────────────────────

    public User() {
        this.statutCompte = StatutCompte.ACTIF;
        this.loginAttempts = 0;
    }

    public User(String nom, String email, String telephone, String motDePasse) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.motDePasse = hashPassword(motDePasse);
        this.statutCompte = StatutCompte.ACTIF;
        this.loginAttempts = 0;
    }

    // ── Authentification & Sécurité ────────────────────────────

    /**
     * Authentifie l'utilisateur avec le mot de passe fourni.
     * Incrémente loginAttempts en cas d'échec.
     * Bloque le compte si loginAttempts > 3.
     *
     * @param mdp le mot de passe en clair
     * @return true si authentifié avec succès
     */
    public boolean authentifier(String mdp) {
        if (this.statutCompte == StatutCompte.BLOQUE) {
            return false;
        }

        String hashedInput = hashPassword(mdp);
        if (this.motDePasse.equals(hashedInput)) {
            resetLoginAttempts();
            return true;
        } else {
            incrementerLoginAttempts();
            return false;
        }
    }

    /**
     * Incrémente le compteur de tentatives de connexion.
     * Si le compteur dépasse 3, le compte est bloqué.
     */
    public void incrementerLoginAttempts() {
        this.loginAttempts++;
        if (this.loginAttempts > 3) {
            this.statutCompte = StatutCompte.BLOQUE;
        }
    }

    /**
     * Réinitialise le compteur de tentatives de connexion.
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }

    // ── Notifications ──────────────────────────────────────────

    /**
     * Envoie une notification simple à l'utilisateur.
     */
    public void notifier(String message) {
        System.out.println("[NOTIFICATION → " + nom + "] " + message);
    }

    /**
     * Envoie une notification multi-format (email + SMS simulé).
     */
    public void notifierMF(String message) {
        System.out.println("[EMAIL → " + email + "] " + message);
        System.out.println("[SMS → " + telephone + "] " + message);
    }

    // ── Hachage SHA-256 ────────────────────────────────────────

    /**
     * Hache un mot de passe en utilisant SHA-256.
     *
     * @param password le mot de passe en clair
     * @return le hash hexadécimal
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // ── Getters & Setters (tous privés → encapsulation) ───────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    /**
     * Sets the password. If the password is already hashed (64 hex chars), 
     * stores it directly. Otherwise, hashes it first.
     */
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    /**
     * Sets the password hash directly without re-hashing.
     * Used when loading from database.
     */
    public void setMotDePasseHashed(String hashedPassword) {
        this.motDePasse = hashedPassword;
    }

    public StatutCompte getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(StatutCompte statutCompte) {
        this.statutCompte = statutCompte;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    /**
     * Retourne le type de l'utilisateur sous forme de chaîne.
     */
    public abstract String getType();

    @Override
    public String toString() {
        return getType() + "{id=" + id + ", nom='" + nom + "', email='" + email + "'}";
    }
}
