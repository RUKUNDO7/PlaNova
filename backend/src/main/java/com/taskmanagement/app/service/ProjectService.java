package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.ProjectRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserService appUserService;

    public ProjectService(ProjectRepository projectRepository, AppUserService appUserService) {
        this.projectRepository = projectRepository;
        this.appUserService = appUserService;
    }

    public List<Project> list(UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.USER) {
            if (viewerId == null) {
                throw new IllegalArgumentException("viewerId is required for user role.");
            }
            return projectRepository.findDistinctByMembers_IdOrOwner_Id(viewerId, viewerId);
        }
        return projectRepository.findAll();
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Project not found with id " + id));
    }

    public Project create(ProjectRequest request, UserRole viewerRole, Long viewerId) {
        AppUser owner = resolveOwner(request.getOwnerId(), viewerRole, viewerId);
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

    public Project update(Long id, ProjectRequest request, UserRole viewerRole, Long viewerId) {
        Project project = findById(id);
        ensureAccess(project, viewerRole, viewerId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getMemberIds() != null) {
            Set<AppUser> members = new HashSet<>(project.getMembers());
            request.getMemberIds().forEach(memberId -> members.add(appUserService.findById(memberId)));
            project.setMembers(members);
        }
        return projectRepository.save(project);
    }

    public void delete(Long id, UserRole viewerRole, Long viewerId) {
        Project project = findById(id);
        ensureAccess(project, viewerRole, viewerId);
        projectRepository.delete(project);
    }

    public Project addMember(Long projectId, Long memberId, UserRole viewerRole, Long viewerId) {
        Project project = findById(projectId);
        ensureAccess(project, viewerRole, viewerId);
        AppUser member = appUserService.findById(memberId);
        project.getMembers().add(member);
        return projectRepository.save(project);
    }

    public Project removeMember(Long projectId, Long memberId, UserRole viewerRole, Long viewerId) {
        Project project = findById(projectId);
        ensureAccess(project, viewerRole, viewerId);
        project.getMembers().removeIf(user -> user.getId().equals(memberId));
        return projectRepository.save(project);
    }

    public void ensureAccess(Project project, UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.ADMIN) {
            return;
        }
        if (viewerId == null) {
            throw new IllegalArgumentException("viewerId is required for user role.");
        }
        boolean member = project.getOwner().getId().equals(viewerId)
            || project.getMembers().stream().anyMatch(user -> user.getId().equals(viewerId));
        if (!member) {
            throw new IllegalArgumentException("You do not have access to this project.");
        }
    }

    private AppUser resolveOwner(Long requestedOwnerId, UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.ADMIN) {
            if (requestedOwnerId == null && viewerId != null) {
                return appUserService.findById(viewerId);
            }
            if (requestedOwnerId == null) {
                throw new IllegalArgumentException("ownerId is required for admin role.");
            }
            return appUserService.findById(requestedOwnerId);
        }
        if (viewerId == null) {
            throw new IllegalArgumentException("viewerId is required for user role.");
        }
        if (requestedOwnerId != null && !requestedOwnerId.equals(viewerId)) {
            throw new IllegalArgumentException("Users can only create projects for themselves.");
        }
        return appUserService.findById(viewerId);
    }
}