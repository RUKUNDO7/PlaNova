package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Project;

import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private int memberCount;
    private LocalDateTime createdAt;

    public static ProjectResponse from(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.id = project.getId();
        response.name = project.getName();
        response.description = project.getDescription();
        response.ownerId = project.getOwner().getId();
        response.ownerName = project.getOwner().getDisplayName();
        response.memberCount = project.getMembers() == null ? 0 : project.getMembers().size();
        response.createdAt = project.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}