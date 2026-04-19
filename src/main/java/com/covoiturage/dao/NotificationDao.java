package com.covoiturage.dao;

import com.covoiturage.model.Notification;
import java.util.List;

public interface NotificationDao {
    void save(Notification notification);
    List<Notification> findByUserId(int userId);
    List<Notification> findUnreadByUserId(int userId);
    void markAsRead(int notificationId);
    void delete(int notificationId);
}
