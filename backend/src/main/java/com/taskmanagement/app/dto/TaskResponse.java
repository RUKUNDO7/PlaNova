package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private boolean completed;
    private String priority;
    private Long ownerId;
    private String ownerName;
    private UserRole ownerRole;

    public static TaskResponse from(Task task) {
        TaskResponse response = new TaskResponse();
        response.id = task.getId();
        response.title = task.getTitle();
        response.description = task.getDescription();
        response.dueDate = task.getDueDate();
        response.createdAt = task.getCreatedAt();
        response.completed = task.isCompleted();
        response.priority = task.getPriority().name();
        response.ownerId = task.getOwner().getId();
        response.ownerName = task.getOwner().getDisplayName();
        response.ownerRole = task.getOwner().getRole();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getPriority() {
        return priority;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public UserRole getOwnerRole() {
        return ownerRole;
    }
}
