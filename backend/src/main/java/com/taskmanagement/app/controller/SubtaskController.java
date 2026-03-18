package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.SubtaskRequest;
import com.taskmanagement.app.dto.SubtaskResponse;
import com.taskmanagement.app.model.Subtask;
import com.taskmanagement.app.service.SubtaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/subtasks")
@CrossOrigin(origins = "http://localhost:5173")
public class SubtaskController {
    
    private final SubtaskService subtaskService;
    
    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    @GetMapping
    public List<SubtaskResponse> getSubtasks(@PathVariable Long taskId) {
        return subtaskService.findByTaskId(taskId).stream()
            .map(SubtaskResponse::from).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubtaskResponse createSubtask(@PathVariable Long taskId, @Valid @RequestBody SubtaskRequest request) {
        Subtask s = subtaskService.create(taskId, request.getTitle());
        return SubtaskResponse.from(s);
    }

    @PutMapping("/{subtaskId}")
    public SubtaskResponse updateSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId, @Valid @RequestBody SubtaskRequest request) {
        Subtask s = subtaskService.update(subtaskId, request.getTitle(), request.getCompleted());
        return SubtaskResponse.from(s);
    }

    @DeleteMapping("/{subtaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId) {
        subtaskService.delete(subtaskId);
    }
}
