package com.covoiturage.model;

import java.time.LocalDateTime;

/**
 * Représente une notification envoyée à un utilisateur.
 */
public class Notification {
    private int id;
    private int userId;
    private String message;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    public Notification() {
        this.dateEnvoi = LocalDateTime.now();
        this.lu = false;
    }

    public Notification(int userId, String message) {
        this.userId = userId;
        this.message = message;
        this.dateEnvoi = LocalDateTime.now();
        this.lu = false;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", message='" + message + '\'' +
                ", dateEnvoi=" + dateEnvoi +
                ", lu=" + lu +
                '}';
    }
}
