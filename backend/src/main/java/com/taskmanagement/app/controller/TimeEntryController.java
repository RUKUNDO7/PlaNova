package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.TimeEntryRequest;
import com.taskmanagement.app.dto.TimeEntryResponse;
import com.taskmanagement.app.model.TimeEntry;
import com.taskmanagement.app.service.TimeEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/time-entries")
@CrossOrigin(origins = "http://localhost:5173")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @GetMapping
    public List<TimeEntryResponse> getTimeEntries(@PathVariable Long taskId) {
        return timeEntryService.findByTaskId(taskId).stream()
            .map(TimeEntryResponse::from).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryResponse logTime(@PathVariable Long taskId,
                                     @Valid @RequestBody TimeEntryRequest request) {
        TimeEntry t = timeEntryService.create(taskId, request.getHours(), request.getDescription(), request.getDate());
        return TimeEntryResponse.from(t);
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTimeEntry(@PathVariable Long taskId,
                                @PathVariable Long entryId) {
        timeEntryService.delete(entryId);
    }
}
