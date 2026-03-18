package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.ActivityResponse;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.service.ActivityService;
import com.taskmanagement.app.service.TaskService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/activity")
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityController {

    private final ActivityService activityService;
    private final TaskService taskService;

    public ActivityController(ActivityService activityService, TaskService taskService) {
        this.activityService = activityService;
        this.taskService = taskService;
    }

    @GetMapping
    public List<ActivityResponse> list(@PathVariable Long taskId) {
        Task task = taskService.findAccessibleById(taskId);
        return activityService.listForTask(task.getId()).stream()
            .map(ActivityResponse::from)
            .collect(Collectors.toList());
    }
}