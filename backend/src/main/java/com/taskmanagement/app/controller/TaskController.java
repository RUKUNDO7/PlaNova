package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.TaskRequest;
import com.taskmanagement.app.dto.TaskResponse;
import com.taskmanagement.app.model.Priority;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getAll(
        @RequestParam(required = false, defaultValue = "all") String status,
        @RequestParam(required = false) Priority priority,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String direction,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        List<Task> tasks = taskService.findAll(status, priority, q, sortBy, direction, viewerRole, viewerId);
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TaskResponse getById(
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Task task = taskService.findAccessibleById(id, viewerRole, viewerId);
        return TaskResponse.from(task);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(
        @Valid @RequestBody TaskRequest taskRequest,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Task created = taskService.create(taskRequest, viewerRole, viewerId);
        return TaskResponse.from(created);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
        @PathVariable Long id,
        @Valid @RequestBody TaskRequest taskRequest,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Task updated = taskService.update(id, taskRequest, viewerRole, viewerId);
        return TaskResponse.from(updated);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse toggleComplete(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> payload,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        boolean completed = payload.getOrDefault("completed", false);
        Task updated = taskService.toggleComplete(id, completed, viewerRole, viewerId);
        return TaskResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        taskService.delete(id, viewerRole, viewerId);
    }

    @DeleteMapping("/completed")
    public Map<String, Long> clearCompleted(
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        long deleted = taskService.deleteCompleted(viewerRole, viewerId);
        return Map.of("deleted", deleted);
    }
}
