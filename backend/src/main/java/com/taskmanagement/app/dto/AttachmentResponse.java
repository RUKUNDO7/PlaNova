package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.TaskAttachment;

import java.time.LocalDateTime;

public class AttachmentResponse {

    private Long id;
    private Long taskId;
    private String name;
    private String url;
    private String type;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    public static AttachmentResponse from(TaskAttachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.id = attachment.getId();
        response.taskId = attachment.getTask().getId();
        response.name = attachment.getName();
        response.url = attachment.getUrl();
        response.type = attachment.getType();
        response.uploadedById = attachment.getUploadedBy().getId();
        response.uploadedByName = attachment.getUploadedBy().getDisplayName();
        response.uploadedAt = attachment.getUploadedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}