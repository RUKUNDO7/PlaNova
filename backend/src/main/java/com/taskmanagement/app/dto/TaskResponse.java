package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean completed;
    private String priority;
    private Long ownerId;
    private String ownerName;
    private UserRole ownerRole;
    private Long columnId;
    private String columnName;
    private Long boardId;
    private Long projectId;
    private List<LabelResponse> labels;

    public static TaskResponse from(Task task) {
        TaskResponse response = new TaskResponse();
        response.id = task.getId();
        response.title = task.getTitle();
        response.description = task.getDescription();
        response.dueDate = task.getDueDate();
        response.createdAt = task.getCreatedAt();
        response.updatedAt = task.getUpdatedAt();
        response.completed = task.isCompleted();
        response.priority = task.getPriority().name();
        response.ownerId = task.getOwner().getId();
        response.ownerName = task.getOwner().getDisplayName();
        response.ownerRole = task.getOwner().getRole();
        if (task.getColumn() != null) {
            response.columnId = task.getColumn().getId();
            response.columnName = task.getColumn().getName();
            response.boardId = task.getColumn().getBoard().getId();
            response.projectId = task.getColumn().getBoard().getProject().getId();
        }
        response.labels = task.getLabels().stream()
            .map(LabelResponse::from)
            .collect(Collectors.toList());
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public Long getColumnId() {
        return columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public Long getBoardId() {
        return boardId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public List<LabelResponse> getLabels() {
        return labels;
    }
}