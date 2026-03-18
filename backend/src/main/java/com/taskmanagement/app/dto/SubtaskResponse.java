package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Subtask;
import java.time.LocalDateTime;

public class SubtaskResponse {
    private Long id;
    private String title;
    private boolean completed;
    private LocalDateTime createdAt;
    
    public static SubtaskResponse from(Subtask s) {
        SubtaskResponse r = new SubtaskResponse();
        r.id = s.getId();
        r.title = s.getTitle();
        r.completed = s.isCompleted();
        r.createdAt = s.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
