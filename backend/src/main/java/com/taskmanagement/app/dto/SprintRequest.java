package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Sprint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class SprintRequest {
    @NotBlank
    @Size(max = 120)
    private String name;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Sprint.Status status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Sprint.Status getStatus() { return status; }
    public void setStatus(Sprint.Status status) { this.status = status; }
}
