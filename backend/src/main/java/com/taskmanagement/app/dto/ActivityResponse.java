package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.TaskActivity;

import java.time.LocalDateTime;

public class ActivityResponse {

    private Long id;
    private Long taskId;
    private String message;
    private String type;
    private LocalDateTime createdAt;

    public static ActivityResponse from(TaskActivity activity) {
        ActivityResponse response = new ActivityResponse();
        response.id = activity.getId();
        response.taskId = activity.getTask().getId();
        response.message = activity.getMessage();
        response.type = activity.getType();
        response.createdAt = activity.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}