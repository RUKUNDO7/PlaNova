package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.TaskRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.BoardColumn;
import com.taskmanagement.app.model.ColumnStatus;
import com.taskmanagement.app.model.NotificationType;
import com.taskmanagement.app.model.Priority;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.TaskLabel;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.BoardColumnRepository;
import com.taskmanagement.app.repository.TaskLabelRepository;
import com.taskmanagement.app.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserService appUserService;
    private final BoardColumnRepository columnRepository;
    private final TaskLabelRepository labelRepository;
    private final ProjectService projectService;
    private final ColumnService columnService;
    private final NotificationService notificationService;
    private final ActivityService activityService;

    public TaskService(TaskRepository taskRepository,
                       AppUserService appUserService,
                       BoardColumnRepository columnRepository,
                       TaskLabelRepository labelRepository,
                       ProjectService projectService,
                       ColumnService columnService,
                       NotificationService notificationService,
                       ActivityService activityService) {
        this.taskRepository = taskRepository;
        this.appUserService = appUserService;
        this.columnRepository = columnRepository;
        this.labelRepository = labelRepository;
        this.projectService = projectService;
        this.columnService = columnService;
        this.notificationService = notificationService;
        this.activityService = activityService;
    }

    public List<Task> findAll(String status,
                              Priority priority,
                              String q,
                              String sortBy,
                              String direction,
                              UserRole viewerRole,
                              Long viewerId,
                              Long projectId,
                              Long boardId,
                              Long columnId,
                              Long labelId) {
        ensureViewerContext(viewerRole, viewerId);
        Specification<Task> spec = Specification.where(null);

        if (StringUtils.hasText(status)) {
            String normalizedStatus = status.toLowerCase(Locale.ROOT);
            if ("active".equals(normalizedStatus)) {
                spec = spec.and((root, query, cb) -> cb.isFalse(root.get("completed")));
            } else if ("completed".equals(normalizedStatus)) {
                spec = spec.and((root, query, cb) -> cb.isTrue(root.get("completed")));
            } else if (!"all".equals(normalizedStatus)) {
                throw new IllegalArgumentException("Invalid status. Use all, active, or completed.");
            }
        }

        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }

        if (StringUtils.hasText(q)) {
            String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like)
            ));
        }

        if (projectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("column").join("board").join("project").get("id"), projectId));
            if (viewerRole == UserRole.USER) {
                Project project = projectService.findById(projectId);
                projectService.ensureAccess(project, viewerRole, viewerId);
            }
        }

        if (boardId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("column").join("board").get("id"), boardId));
        }

        if (columnId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("column").get("id"), columnId));
        }

        if (labelId != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(root.join("labels").get("id"), labelId);
            });
        }

        spec = applyOwnershipFilter(spec, viewerRole, viewerId, projectId);

        Sort sort = buildSort(sortBy, direction);
        return taskRepository.findAll(spec, sort);
    }

    public Task findAccessibleById(Long id, UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        Task task = findById(id);
        ensureAccess(task, viewerRole, viewerId);
        return task;
    }

    public Task create(TaskRequest request, UserRole viewerRole, Long viewerId) {
        AppUser owner = resolveOwnerForCreate(request.getOwnerId(), viewerRole, viewerId);
        BoardColumn column = resolveColumn(request.getColumnId());
        Project project = projectFromColumn(column);
        projectService.ensureAccess(project, viewerRole, viewerId);
        ensureProjectMember(owner, project);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setOwner(owner);
        task.setColumn(column);
        task.setLabels(resolveLabels(request.getLabelIds(), project));

        boolean completed = Boolean.TRUE.equals(request.getCompleted());
        if (column != null && column.getStatus() == ColumnStatus.DONE) {
            completed = true;
        }
        task.setCompleted(completed);

        Task saved = taskRepository.save(task);
        activityService.log(saved, "Task created", "TASK");

        notifyAssignment(saved, viewerRole, viewerId, owner);
        return saved;
    }

    public Task update(Long id, TaskRequest request, UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        Task existing = findById(id);
        ensureAccess(existing, viewerRole, viewerId);

        BoardColumn column = resolveColumn(request.getColumnId());
        Project project = projectFromColumn(column != null ? column : existing.getColumn());
        projectService.ensureAccess(project, viewerRole, viewerId);

        AppUser previousOwner = existing.getOwner();
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setDueDate(request.getDueDate());
        existing.setPriority(request.getPriority());
        if (request.getCompleted() != null) {
            existing.setCompleted(request.getCompleted());
        }

        AppUser owner = resolveOwnerForUpdate(existing, request, viewerRole);
        ensureProjectMember(owner, project);
        existing.setOwner(owner);

        if (column != null) {
            existing.setColumn(column);
            if (column.getStatus() == ColumnStatus.DONE) {
                existing.setCompleted(true);
            }
        }

        existing.setLabels(resolveLabels(request.getLabelIds(), project));

        Task saved = taskRepository.save(existing);

        if (column != null) {
            activityService.log(saved, "Moved to " + column.getName(), "MOVE");
        }
        if (!previousOwner.getId().equals(owner.getId())) {
            activityService.log(saved, "Reassigned to " + owner.getDisplayName(), "ASSIGN");
            notificationService.create(owner, NotificationType.ASSIGNMENT, "You were assigned to " + saved.getTitle(), saved);
        }
        if (request.getCompleted() != null && request.getCompleted()) {
            notificationService.create(owner, NotificationType.COMPLETED, "Task completed: " + saved.getTitle(), saved);
        }

        return saved;
    }

    public Task toggleComplete(Long id, boolean completed, UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        Task existing = findById(id);
        ensureAccess(existing, viewerRole, viewerId);
        existing.setCompleted(completed);

        if (existing.getColumn() != null) {
            Long boardId = existing.getColumn().getBoard().getId();
            if (completed) {
                BoardColumn done = columnService.findDoneColumn(boardId);
                if (done != null) {
                    existing.setColumn(done);
                }
            } else {
                BoardColumn todo = columnService.findTodoColumn(boardId);
                if (todo != null) {
                    existing.setColumn(todo);
                }
            }
        }

        Task saved = taskRepository.save(existing);
        activityService.log(saved, completed ? "Marked complete" : "Marked active", "STATUS");
        return saved;
    }

    public void delete(Long id, UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        Task existing = findById(id);
        ensureAccess(existing, viewerRole, viewerId);
        taskRepository.delete(existing);
    }

    public long deleteCompleted(UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        if (viewerRole == UserRole.ADMIN) {
            return taskRepository.deleteByCompletedTrue();
        }
        return taskRepository.deleteByCompletedTrueAndOwnerId(viewerId);
    }

    private Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
    }

    private void ensureViewerContext(UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.USER && viewerId == null) {
            throw new IllegalArgumentException("ViewerId is required for user role.");
        }
    }

    private void ensureAccess(Task task, UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.ADMIN) {
            return;
        }
        if (viewerId == null) {
            throw new IllegalArgumentException("ViewerId is required for user role.");
        }
        if (task.getOwner() != null && task.getOwner().getId().equals(viewerId)) {
            return;
        }
        Project project = projectFromColumn(task.getColumn());
        projectService.ensureAccess(project, viewerRole, viewerId);
    }

    private Specification<Task> applyOwnershipFilter(Specification<Task> spec, UserRole viewerRole, Long viewerId, Long projectId) {
        if (viewerRole == UserRole.USER && projectId == null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("owner").get("id"), viewerId));
        }
        return spec;
    }

    private AppUser resolveOwnerForCreate(Long requestedOwnerId, UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.ADMIN) {
            if (requestedOwnerId == null) {
                throw new IllegalArgumentException("ownerId is required for admin role.");
            }
            return appUserService.findById(requestedOwnerId);
        }
        if (requestedOwnerId != null && !requestedOwnerId.equals(viewerId)) {
            throw new IllegalArgumentException("Users can only create tasks for themselves.");
        }
        return appUserService.findById(viewerId);
    }

    private AppUser resolveOwnerForUpdate(Task existing, TaskRequest request, UserRole viewerRole) {
        if (viewerRole == UserRole.ADMIN && request.getOwnerId() != null && !request.getOwnerId().equals(existing.getOwner().getId())) {
            return appUserService.findById(request.getOwnerId());
        }
        return existing.getOwner();
    }

    private BoardColumn resolveColumn(Long columnId) {
        if (columnId == null) {
            return null;
        }
        return columnRepository.findById(columnId)
            .orElseThrow(() -> new EntityNotFoundException("Column not found with id " + columnId));
    }

    private Set<TaskLabel> resolveLabels(List<Long> labelIds, Project project) {
        if (labelIds == null) {
            return new HashSet<>();
        }
        Set<TaskLabel> labels = new HashSet<>(labelRepository.findAllById(labelIds));
        for (TaskLabel label : labels) {
            if (!label.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Label does not belong to project.");
            }
        }
        return labels;
    }

    private Project projectFromColumn(BoardColumn column) {
        if (column == null || column.getBoard() == null) {
            throw new IllegalArgumentException("Task must belong to a board column.");
        }
        return column.getBoard().getProject();
    }

    private void ensureProjectMember(AppUser user, Project project) {
        boolean member = project.getOwner().getId().equals(user.getId())
            || project.getMembers().stream().anyMatch(memberUser -> memberUser.getId().equals(user.getId()));
        if (!member) {
            throw new IllegalArgumentException("Assignee must be a member of the project.");
        }
    }

    private void notifyAssignment(Task task, UserRole viewerRole, Long viewerId, AppUser owner) {
        if (viewerRole == UserRole.USER && viewerId != null && owner.getId().equals(viewerId)) {
            return;
        }
        notificationService.create(owner, NotificationType.ASSIGNMENT, "You were assigned to " + task.getTitle(), task);
    }

    private Sort buildSort(String sortBy, String direction) {
        String normalizedSortBy = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        String normalizedDirection = StringUtils.hasText(direction) ? direction : "desc";

        List<String> allowedFields = List.of("createdAt", "dueDate", "priority", "title", "updatedAt");
        if (!allowedFields.contains(normalizedSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field.");
        }

        Sort.Direction sortDirection;
        if ("asc".equalsIgnoreCase(normalizedDirection)) {
            sortDirection = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(normalizedDirection)) {
            sortDirection = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Invalid direction. Use asc or desc.");
        }

        return Sort.by(sortDirection, normalizedSortBy);
    }
}