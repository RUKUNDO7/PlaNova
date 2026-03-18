package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Sprint;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SprintResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Sprint.Status status;
    private Long boardId;
    private LocalDateTime createdAt;
    
    public static SprintResponse from(Sprint s) {
        SprintResponse r = new SprintResponse();
        r.id = s.getId();
        r.name = s.getName();
        r.startDate = s.getStartDate();
        r.endDate = s.getEndDate();
        r.status = s.getStatus();
        r.boardId = s.getBoard().getId();
        r.createdAt = s.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Sprint.Status getStatus() { return status; }
    public Long getBoardId() { return boardId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
