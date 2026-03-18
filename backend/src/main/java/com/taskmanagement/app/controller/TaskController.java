package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.TaskRequest;
import com.taskmanagement.app.dto.TaskResponse;
import com.taskmanagement.app.model.Priority;
import com.taskmanagement.app.model.Task;
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
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) Long boardId,
        @RequestParam(required = false) Long columnId,
        @RequestParam(required = false) Long labelId,
        @RequestParam(required = false) Boolean archived
    ) {
        List<Task> tasks = taskService.findAll(status, priority, q, sortBy, direction, projectId, boardId, columnId, labelId, archived);
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        Task task = taskService.findAccessibleById(id);
        return TaskResponse.from(task);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest taskRequest) {
        Task created = taskService.create(taskRequest);
        return TaskResponse.from(created);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest taskRequest) {
        Task updated = taskService.update(id, taskRequest);
        return TaskResponse.from(updated);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse toggleComplete(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        boolean completed = payload.getOrDefault("completed", false);
        Task updated = taskService.toggleComplete(id, completed);
        return TaskResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @PostMapping("/{id}/dependencies/{dependsOnId}")
    public TaskResponse addDependency(@PathVariable Long id, @PathVariable Long dependsOnId) {
        Task t = taskService.addDependency(id, dependsOnId);
        return TaskResponse.from(t);
    }

    @DeleteMapping("/{id}/dependencies/{dependsOnId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDependency(@PathVariable Long id, @PathVariable Long dependsOnId) {
        taskService.removeDependency(id, dependsOnId);
    }

    @DeleteMapping("/completed")
    public Map<String, Long> clearCompleted() {
        long deleted = taskService.deleteCompleted();
        return Map.of("deleted", deleted);
    }
}