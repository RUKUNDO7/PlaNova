package com.taskmanagement.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubtaskRequest {
    @NotBlank
    @Size(max = 200)
    private String title;
    
    private Boolean completed;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
