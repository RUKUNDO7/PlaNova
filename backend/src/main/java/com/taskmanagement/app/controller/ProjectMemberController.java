package com.taskmanagement.app.controller;

import com.taskmanagement.app.model.ProjectMember;
import com.taskmanagement.app.service.ProjectMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public List<ProjectMember> getMembers(@PathVariable Long projectId) {
        return projectMemberService.findMembers(projectId);
    }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMember addMember(@PathVariable Long projectId,
                                   @PathVariable Long userId,
                                   @RequestParam ProjectMember.Role role) {
        return projectMemberService.addMember(projectId, userId, role);
    }

    @PutMapping("/{userId}/role")
    public ProjectMember updateRole(@PathVariable Long projectId,
                                    @PathVariable Long userId,
                                    @RequestParam ProjectMember.Role role) {
        return projectMemberService.updateRole(projectId, userId, role);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long projectId,
                             @PathVariable Long userId) {
        projectMemberService.removeMember(projectId, userId);
    }
}
