package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.TaskLabel;

public class LabelResponse {

    private Long id;
    private String name;
    private String color;
    private Long projectId;

    public static LabelResponse from(TaskLabel label) {
        LabelResponse response = new LabelResponse();
        response.id = label.getId();
        response.name = label.getName();
        response.color = label.getColor();
        response.projectId = label.getProject().getId();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Long getProjectId() {
        return projectId;
    }
}