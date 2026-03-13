package com.taskmanagement.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ProjectRequest {

    @NotBlank
    @Size(max = 140)
    private String name;

    @Size(max = 400)
    private String description;

    private Long ownerId;

    private List<Long> memberIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}