package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.TaskComment;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private Long taskId;
    private String body;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;

    public static CommentResponse from(TaskComment comment) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.taskId = comment.getTask().getId();
        response.body = comment.getBody();
        response.authorId = comment.getAuthor().getId();
        response.authorName = comment.getAuthor().getDisplayName();
        response.createdAt = comment.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getBody() {
        return body;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}