package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.ProjectRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.repository.ProjectRepository;
import com.taskmanagement.app.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserService appUserService;
    private final SecurityService securityService;

    public ProjectService(ProjectRepository projectRepository, AppUserService appUserService, SecurityService securityService) {
        this.projectRepository = projectRepository;
        this.appUserService = appUserService;
        this.securityService = securityService;
    }

    public List<Project> list() {
        if (!securityService.isAdmin()) {
            Long userId = securityService.getCurrentUserId();
            return projectRepository.findDistinctByMembers_IdOrOwner_Id(userId, userId);
        }
        return projectRepository.findAll();
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Project not found with id " + id));
    }

    public Project create(ProjectRequest request) {
        AppUser owner = resolveOwner(request.getOwnerId());
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        Set<AppUser> members = new HashSet<>();
        members.add(owner);
        if (request.getMemberIds() != null) {
            request.getMemberIds().forEach(id -> members.add(appUserService.findById(id)));
        }
        project.setMembers(members);
        return projectRepository.save(project);
    }

    public Project update(Long id, ProjectRequest request) {
        Project project = findById(id);
        ensureAccess(project);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getMemberIds() != null) {
            Set<AppUser> members = new HashSet<>(project.getMembers());
            request.getMemberIds().forEach(memberId -> members.add(appUserService.findById(memberId)));
            project.setMembers(members);
        }
        return projectRepository.save(project);
    }

    public void delete(Long id) {
        Project project = findById(id);
        ensureAccess(project);
        projectRepository.delete(project);
    }

    public Project addMember(Long projectId, Long memberId) {
        Project project = findById(projectId);
        ensureAccess(project);
        AppUser member = appUserService.findById(memberId);
        project.getMembers().add(member);
        return projectRepository.save(project);
    }

    public Project removeMember(Long projectId, Long memberId) {
        Project project = findById(projectId);
        ensureAccess(project);
        project.getMembers().removeIf(user -> user.getId().equals(memberId));
        return projectRepository.save(project);
    }

    public void ensureAccess(Project project) {
        if (securityService.isAdmin()) {
            return;
        }
        Long userId = securityService.getCurrentUserId();
        boolean member = project.getOwner().getId().equals(userId)
            || project.getMembers().stream().anyMatch(user -> user.getId().equals(userId));
        if (!member) {
            throw new IllegalArgumentException("You do not have access to this project.");
        }
    }

    private AppUser resolveOwner(Long requestedOwnerId) {
        Long currentUserId = securityService.getCurrentUserId();
        if (securityService.isAdmin()) {
            if (requestedOwnerId == null) {
                return appUserService.findById(currentUserId);
            }
            return appUserService.findById(requestedOwnerId);
        }
        if (requestedOwnerId != null && !requestedOwnerId.equals(currentUserId)) {
            throw new IllegalArgumentException("Users can only create projects for themselves.");
        }
        return appUserService.findById(currentUserId);
    }
}