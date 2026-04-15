package com.covoiturage.service;

import com.covoiturage.model.User;

/**
 * Service de notification (simulation console).
 */
public class NotificationService {

    /**
     * Envoie une notification à un utilisateur.
     */
    public void envoyerNotification(User user, String message) {
        user.notifier(message);
    }

    /**
     * Envoie une notification multi-format (email + SMS simulé).
     */
    public void envoyerNotificationMF(User user, String message) {
        user.notifierMF(message);
    }
}
