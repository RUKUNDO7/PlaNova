package com.taskmanagement.app.controller;

import com.taskmanagement.app.security.SecurityService;
import com.taskmanagement.app.repository.ProjectRepository;
import com.taskmanagement.app.repository.TaskRepository;
import com.taskmanagement.app.repository.AppUserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final AppUserRepository userRepository;
    private final SecurityService securityService;

    public ReportController(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            AppUserRepository userRepository,
                            SecurityService securityService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    @GetMapping("/system-summary")
    public Map<String, Object> getSystemSummary() {
        if (!securityService.isAdmin()) {
            throw new IllegalArgumentException("Only admins can access system wide reports");
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalUsers", userRepository.count());
        report.put("totalProjects", projectRepository.count());
        report.put("totalTasks", taskRepository.count());
        report.put("completedTasks", taskRepository.countByCompletedTrue());

        return report;
    }
}
