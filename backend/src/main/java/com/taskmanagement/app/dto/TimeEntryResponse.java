package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.TimeEntry;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeEntryResponse {
    private Long id;
    private Double hours;
    private String description;
    private LocalDate date;
    private UserSummaryResponse user;
    private LocalDateTime createdAt;
    
    public static TimeEntryResponse from(TimeEntry t) {
        TimeEntryResponse r = new TimeEntryResponse();
        r.id = t.getId();
        r.hours = t.getHours();
        r.description = t.getDescription();
        r.date = t.getDate();
        r.user = UserSummaryResponse.from(t.getUser());
        r.createdAt = t.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Double getHours() { return hours; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public UserSummaryResponse getUser() { return user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
