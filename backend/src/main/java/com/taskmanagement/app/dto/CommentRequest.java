package com.taskmanagement.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CommentRequest {

    @NotBlank
    @Size(max = 800)
    private String body;

    private Long authorId;

    private List<Long> mentionIds;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public List<Long> getMentionIds() {
        return mentionIds;
    }

    public void setMentionIds(List<Long> mentionIds) {
        this.mentionIds = mentionIds;
    }
}