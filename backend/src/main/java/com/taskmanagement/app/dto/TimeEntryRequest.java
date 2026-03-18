package com.taskmanagement.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class TimeEntryRequest {
    @NotNull
    @DecimalMin("0.0")
    private Double hours;
    
    @Size(max = 300)
    private String description;
    
    private LocalDate date;

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
