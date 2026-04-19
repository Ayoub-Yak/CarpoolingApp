package com.covoiturage.service;

import com.covoiturage.dao.NotificationDao;
import com.covoiturage.dao.NotificationDaoImpl;
import com.covoiturage.model.Notification;
import com.covoiturage.model.User;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;

/**
 * Service de notification avec persistance en base de données.
 */
public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService() {
        this.notificationDao = new NotificationDaoImpl();
    }

    /**
     * Envoie une notification à un utilisateur et la sauvegarde.
     */
    public void envoyerNotification(User user, String message) {
        envoyerNotification(user.getId(), message);
        // Simulation console
        user.notifier(message);
    }

    /**
     * Envoie une notification via userId.
     */
    public void envoyerNotification(int userId, String message) {
        Notification notification = new Notification(userId, message);
        notificationDao.save(notification);
    }

    /**
     * Envoie une notification multi-format et la sauvegarde.
     */
    public void envoyerNotificationMF(User user, String message) {
        Notification notification = new Notification(user.getId(), message);
        notificationDao.save(notification);

        // Simulation console
        user.notifierMF(message);
    }

    public List<Notification> getUnreadNotifications(int userId) {
        return notificationDao.findUnreadByUserId(userId);
    }

    public List<Notification> getAllNotifications(int userId) {
        return notificationDao.findByUserId(userId);
    }

    public void marquerCommeLu(int notificationId) {
        notificationDao.markAsRead(notificationId);
    }

    /**
     * Affiche une alerte JavaFX simple.
     */
    public static void showPopup(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
