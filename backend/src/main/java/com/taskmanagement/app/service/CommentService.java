package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.CommentRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.NotificationType;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.TaskComment;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.TaskCommentRepository;
import com.taskmanagement.app.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final AppUserService appUserService;
    private final ProjectService projectService;
    private final ActivityService activityService;
    private final NotificationService notificationService;

    public CommentService(TaskCommentRepository commentRepository,
                          TaskRepository taskRepository,
                          AppUserService appUserService,
                          ProjectService projectService,
                          ActivityService activityService,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.appUserService = appUserService;
        this.projectService = projectService;
        this.activityService = activityService;
        this.notificationService = notificationService;
    }

    public List<TaskComment> list(Long taskId, UserRole viewerRole, Long viewerId) {
        Task task = findTask(taskId);
        Project project = getProject(task);
        projectService.ensureAccess(project, viewerRole, viewerId);
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    public TaskComment create(Long taskId, CommentRequest request, UserRole viewerRole, Long viewerId) {
        Task task = findTask(taskId);
        Project project = getProject(task);
        projectService.ensureAccess(project, viewerRole, viewerId);
        AppUser author = resolveAuthor(request.getAuthorId(), viewerRole, viewerId);

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setBody(request.getBody());
        TaskComment saved = commentRepository.save(comment);

        activityService.log(task, author.getDisplayName() + " commented", "COMMENT");
        notifyParticipants(task, author, request.getMentionIds());

        return saved;
    }

    private void notifyParticipants(Task task, AppUser author, List<Long> mentionIds) {
        Set<Long> notified = new HashSet<>();
        if (task.getOwner() != null && !task.getOwner().getId().equals(author.getId())) {
            notificationService.create(task.getOwner(), NotificationType.COMMENT,
                author.getDisplayName() + " commented on " + task.getTitle(), task);
            notified.add(task.getOwner().getId());
        }
        if (mentionIds != null) {
            for (Long mentionId : mentionIds) {
                if (mentionId == null || mentionId.equals(author.getId()) || notified.contains(mentionId)) {
                    continue;
                }
                AppUser mentioned = appUserService.findById(mentionId);
                notificationService.create(mentioned, NotificationType.MENTION,
                    author.getDisplayName() + " mentioned you on " + task.getTitle(), task);
            }
        }
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

    private AppUser resolveAuthor(Long authorId, UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.ADMIN) {
            if (authorId == null) {
                throw new IllegalArgumentException("authorId is required for admin role.");
            }
            return appUserService.findById(authorId);
        }
        if (viewerId == null) {
            throw new IllegalArgumentException("viewerId is required for user role.");
        }
        if (authorId != null && !authorId.equals(viewerId)) {
            throw new IllegalArgumentException("Users can only comment as themselves.");
        }
        return appUserService.findById(viewerId);
    }
}