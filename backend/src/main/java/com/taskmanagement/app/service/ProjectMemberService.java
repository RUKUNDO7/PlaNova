package com.taskmanagement.app.service;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.ProjectMember;
import com.taskmanagement.app.security.SecurityService;
import com.taskmanagement.app.repository.ProjectMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final AppUserService appUserService;
    private final SecurityService securityService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,
                                ProjectService projectService,
                                AppUserService appUserService,
                                SecurityService securityService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectService = projectService;
        this.appUserService = appUserService;
        this.securityService = securityService;
    }

    public List<ProjectMember> findMembers(Long projectId) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project);
        return projectMemberRepository.findByProjectId(projectId);
    }

    public ProjectMember addMember(Long projectId, Long userId, ProjectMember.Role role) {
        Project project = projectService.findById(projectId);
        Long currentUserId = securityService.getCurrentUserId();
        if (!securityService.isAdmin() && !project.getOwner().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Only project owners and admins can add members");
        }
        
        AppUser user = appUserService.findById(userId);
        
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new IllegalArgumentException("User is already a project member");
        }
        
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(role != null ? role : ProjectMember.Role.CONTRIBUTOR);
        
        // Also add to the legacy set for backward compatibility
        project.getMembers().add(user);
        
        return projectMemberRepository.save(member);
    }

    public ProjectMember updateRole(Long projectId, Long userId, ProjectMember.Role role) {
        Project project = projectService.findById(projectId);
        Long currentUserId = securityService.getCurrentUserId();
        if (!securityService.isAdmin() && !project.getOwner().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Only project owners and admins can update member roles");
        }
        
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Project member not found"));
            
        member.setRole(role);
        return projectMemberRepository.save(member);
    }

    public void removeMember(Long projectId, Long userId) {
        Project project = projectService.findById(projectId);
        Long currentUserId = securityService.getCurrentUserId();
        if (!securityService.isAdmin() && !project.getOwner().getId().equals(currentUserId) && !userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Only project owners and admins can remove other members");
        }
        
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Project member not found"));
            
        project.getMembers().remove(member.getUser());
        projectMemberRepository.delete(member);
    }
}
