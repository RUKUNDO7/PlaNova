package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.ProjectRequest;
import com.taskmanagement.app.dto.ProjectResponse;
import com.taskmanagement.app.dto.UserSummaryResponse;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list(
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        return projectService.list(viewerRole, viewerId).stream()
            .map(ProjectResponse::from)
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
        @Valid @RequestBody ProjectRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Project created = projectService.create(request, viewerRole, viewerId);
        return ProjectResponse.from(created);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(
        @PathVariable Long id,
        @Valid @RequestBody ProjectRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Project updated = projectService.update(id, request, viewerRole, viewerId);
        return ProjectResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        projectService.delete(id, viewerRole, viewerId);
    }

    @GetMapping("/{id}/members")
    public List<UserSummaryResponse> members(
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Project project = projectService.findById(id);
        projectService.ensureAccess(project, viewerRole, viewerId);
        return project.getMembers().stream()
            .map(UserSummaryResponse::from)
            .collect(Collectors.toList());
    }

    @PostMapping("/{id}/members")
    public ProjectResponse addMember(
        @PathVariable Long id,
        @RequestBody Map<String, Long> payload,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Long memberId = payload.get("userId");
        if (memberId == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        Project updated = projectService.addMember(id, memberId, viewerRole, viewerId);
        return ProjectResponse.from(updated);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ProjectResponse removeMember(
        @PathVariable Long id,
        @PathVariable Long memberId,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Project updated = projectService.removeMember(id, memberId, viewerRole, viewerId);
        return ProjectResponse.from(updated);
    }
}