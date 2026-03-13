package com.taskmanagement.app.service;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Notification;
import com.taskmanagement.app.model.NotificationType;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification create(AppUser recipient, NotificationType type, String message, Task task) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setTask(task);
        return notificationRepository.save(notification);
    }

    public List<Notification> list(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    public Notification markRead(Long id, Long recipientId) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found with id " + id));
        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new IllegalArgumentException("Cannot update notifications for another user.");
        }
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public int markAllRead(Long recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        int updated = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                updated++;
            }
        }
        notificationRepository.saveAll(notifications);
        return updated;
    }
}