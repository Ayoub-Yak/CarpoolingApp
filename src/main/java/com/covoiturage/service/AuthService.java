package com.covoiturage.service;

import com.covoiturage.dao.UserDao;
import com.covoiturage.dao.UserDaoImpl;
import com.covoiturage.model.*;
import com.covoiturage.model.enums.StatutCompte;

/**
 * Service d'authentification : login, inscription, gestion des comptes.
 */
public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDaoImpl();
    }

    /**
     * Crée un nouveau compte utilisateur (Passager ou Chauffeur).
     *
     * @throws IllegalArgumentException si l'email est déjà utilisé
     */
    public void creerCompte(User user) {
        if (userDao.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }
        userDao.save(user);
    }

    /**
     * Authentifie un utilisateur par email et mot de passe.
     * Gère le compteur de tentatives et le blocage automatique.
     *
     * @return l'utilisateur authentifié, ou null si échec
     * @throws IllegalStateException si le compte est bloqué ou suspendu
     */
    public User authentifierParEmail(String email, String motDePasse) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            return null;
        }

        if (user.getStatutCompte() == StatutCompte.BLOQUE) {
            throw new IllegalStateException("Votre compte est bloqué. Contactez l'administrateur.");
        }

        if (user.getStatutCompte() == StatutCompte.SUSPENDU) {
            throw new IllegalStateException("Votre compte est suspendu. Contactez l'administrateur.");
        }

        if (user.authentifier(motDePasse)) {
            userDao.update(user); // Reset login attempts
            return user;
        } else {
            userDao.update(user); // Save incremented login attempts
            if (user.getStatutCompte() == StatutCompte.BLOQUE) {
                throw new IllegalStateException(
                    "Trop de tentatives échouées. Votre compte a été bloqué."
                );
            }
            return null;
        }
    }

    /**
     * Prépare le blocage d'un utilisateur (par un admin).
     */
    public void bloquerUtilisateur(User user) {
        user.setStatutCompte(StatutCompte.BLOQUE);
        userDao.update(user);
    }

    /**
     * Suspend un compte utilisateur (par un admin).
     */
    public void suspendreCompte(Admin admin, User user) {
        user.setStatutCompte(StatutCompte.SUSPENDU);
        userDao.update(user);
    }

    /**
     * Réactive un compte bloqué ou suspendu (par un admin).
     */
    public void reactiverCompte(User user) {
        user.setStatutCompte(StatutCompte.ACTIF);
        user.resetLoginAttempts();
        userDao.update(user);
    }

    public User findById(int id) {
        return userDao.findById(id);
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public java.util.List<User> findAll() {
        return userDao.findAll();
    }

    public java.util.List<User> findByType(String type) {
        return userDao.findByType(type);
    }
}
