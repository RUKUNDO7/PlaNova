package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.AttachmentRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.NotificationType;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.TaskAttachment;
import com.taskmanagement.app.repository.TaskAttachmentRepository;
import com.taskmanagement.app.security.SecurityService;
import com.taskmanagement.app.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final AppUserService appUserService;
    private final ProjectService projectService;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final SecurityService securityService;

    public AttachmentService(TaskAttachmentRepository attachmentRepository,
                             TaskRepository taskRepository,
                             AppUserService appUserService,
                             ProjectService projectService,
                             ActivityService activityService,
                             NotificationService notificationService,
                             SecurityService securityService) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.appUserService = appUserService;
        this.projectService = projectService;
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.securityService = securityService;
    }

    public List<TaskAttachment> list(Long taskId) {
        Task task = findTask(taskId);
        Project project = getProject(task);
        projectService.ensureAccess(project);
        return attachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
    }

    public TaskAttachment create(Long taskId, AttachmentRequest request) {
        Task task = findTask(taskId);
        Project project = getProject(task);
        projectService.ensureAccess(project);
        AppUser uploader = resolveUploader(request.getUploadedById());

        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setUploadedBy(uploader);
        attachment.setName(request.getName());
        attachment.setUrl(request.getUrl());
        attachment.setType(request.getType());
        TaskAttachment saved = attachmentRepository.save(attachment);

        activityService.log(task, uploader.getDisplayName() + " added an attachment", "ATTACHMENT");
        if (task.getOwner() != null && !task.getOwner().getId().equals(uploader.getId())) {
            notificationService.create(task.getOwner(), NotificationType.ATTACHMENT,
                uploader.getDisplayName() + " added an attachment to " + task.getTitle(), task);
        }

        return saved;
    }

    public void delete(Long taskId, Long attachmentId) {
        Task task = findTask(taskId);
        Project project = getProject(task);
        projectService.ensureAccess(project);
        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id " + attachmentId));
        if (!attachment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Attachment does not belong to task.");
        }
        attachmentRepository.delete(attachment);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
    }

    private Project getProject(Task task) {
        if (task.getColumn() == null || task.getColumn().getBoard() == null) {
            throw new IllegalArgumentException("Task is not assigned to a project board.");
        }
        return task.getColumn().getBoard().getProject();
    }

    private AppUser resolveUploader(Long uploaderId) {
        Long currentUserId = securityService.getCurrentUserId();
        if (securityService.isAdmin()) {
            if (uploaderId == null) {
                return appUserService.findById(currentUserId);
            }
            return appUserService.findById(uploaderId);
        }
        if (uploaderId != null && !uploaderId.equals(currentUserId)) {
            throw new IllegalArgumentException("Users can only upload as themselves.");
        }
        return appUserService.findById(currentUserId);
    }
}