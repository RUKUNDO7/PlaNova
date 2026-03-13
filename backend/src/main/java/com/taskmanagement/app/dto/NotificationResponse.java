package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;
    private Long taskId;

    public static NotificationResponse from(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.id = notification.getId();
        response.message = notification.getMessage();
        response.type = notification.getType().name();
        response.read = notification.isRead();
        response.createdAt = notification.getCreatedAt();
        response.taskId = notification.getTask() == null ? null : notification.getTask().getId();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getTaskId() {
        return taskId;
    }
}