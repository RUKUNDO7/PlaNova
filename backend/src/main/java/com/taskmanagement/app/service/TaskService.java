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
import com.taskmanagement.app.repository.BoardColumnRepository;
import com.taskmanagement.app.repository.NotificationRepository;
import com.taskmanagement.app.security.SecurityService;
import com.taskmanagement.app.repository.SprintRepository;
import com.taskmanagement.app.repository.TaskLabelRepository;
import com.taskmanagement.app.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final NotificationRepository notificationRepository;
    private final ActivityService activityService;
    private final SprintRepository sprintRepository;
    private final SecurityService securityService;

    public TaskService(TaskRepository taskRepository,
                       AppUserService appUserService,
                       BoardColumnRepository columnRepository,
                       TaskLabelRepository labelRepository,
                       ProjectService projectService,
                       ColumnService columnService,
                       NotificationService notificationService,
                       NotificationRepository notificationRepository,
                       ActivityService activityService,
                       SprintRepository sprintRepository,
                       SecurityService securityService) {
        this.taskRepository = taskRepository;
        this.appUserService = appUserService;
        this.columnRepository = columnRepository;
        this.labelRepository = labelRepository;
        this.projectService = projectService;
        this.columnService = columnService;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.activityService = activityService;
        this.sprintRepository = sprintRepository;
        this.securityService = securityService;
    }

    public List<Task> findAll(String status,
                              Priority priority,
                              String q,
                              String sortBy,
                              String direction,
                              Long projectId,
                              Long boardId,
                              Long columnId,
                              Long labelId,
                              Boolean archived) {
        Specification<Task> spec = Specification.where(null);

        if (archived != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("archived"), archived));
        } else {
            // By default, don't show archived tasks unless requested
            spec = spec.and((root, query, cb) -> cb.equal(root.get("archived"), false));
        }

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
            if (!securityService.isAdmin()) {
                Project project = projectService.findById(projectId);
                projectService.ensureAccess(project);
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

        spec = applyOwnershipFilter(spec, projectId);

        // Ensure all associations required for API serialization are fetched while the
        // persistence context is open (open-in-view is disabled).
        spec = spec.and(fetchAssociations());

        Sort sort = buildSort(sortBy, direction);
        return taskRepository.findAll(spec, sort);
    }

    private Specification<Task> fetchAssociations() {
        return (root, query, cb) -> {
            // Avoid fetch joins for count queries.
            Class<?> resultType = query.getResultType();
            if (resultType != Long.class && resultType != long.class) {
                root.fetch("owner", JoinType.LEFT);

                Fetch<Object, Object> columnFetch = root.fetch("column", JoinType.LEFT);
                Fetch<Object, Object> boardFetch = columnFetch.fetch("board", JoinType.LEFT);
                boardFetch.fetch("project", JoinType.LEFT);

                root.fetch("labels", JoinType.LEFT);
                root.fetch("assignees", JoinType.LEFT);
                root.fetch("subtasks", JoinType.LEFT);
                root.fetch("dependencies", JoinType.LEFT);
                root.fetch("sprint", JoinType.LEFT);

                query.distinct(true);
            }

            return cb.conjunction();
        };
    }

    public Task findAccessibleById(Long id) {
        Task task = findById(id);
        ensureAccess(task);
        return task;
    }

    public Task create(TaskRequest request) {
        AppUser owner = resolveOwnerForCreate(request.getOwnerId());
        BoardColumn column = resolveColumn(request.getColumnId());
        Project project = projectFromColumn(column);
        projectService.ensureAccess(project);
        ensureProjectMember(owner, project);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setOwner(owner);
        task.setColumn(column);
        task.setLabels(resolveLabels(request.getLabelIds(), project));
        task.setAssignees(resolveAssignees(request.getAssigneeIds(), project));
        task.setEstimatedHours(request.getEstimatedHours());
        task.setRecurrenceRule(request.getRecurrenceRule());
        
        if (request.getSprintId() != null) {
            task.setSprint(sprintRepository.findById(request.getSprintId())
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found")));
        }

        boolean completed = Boolean.TRUE.equals(request.getCompleted());
        if (column != null && column.getStatus() == ColumnStatus.DONE) {
            completed = true;
        }
        task.setCompleted(completed);

        Task saved = taskRepository.save(task);
        activityService.log(saved, "Task created", "TASK");

        notifyAssignment(saved, owner);
        for (AppUser assignee : saved.getAssignees()) {
            if (!assignee.getId().equals(owner.getId())) {
                notifyAssignment(saved, assignee);
            }
        }
        return saved;
    }

    public Task update(Long id, TaskRequest request) {
        Task existing = findById(id);
        ensureAccess(existing);

        BoardColumn column = resolveColumn(request.getColumnId());
        Project project = projectFromColumn(column != null ? column : existing.getColumn());
        projectService.ensureAccess(project);

        AppUser previousOwner = existing.getOwner();
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setDueDate(request.getDueDate());
        existing.setPriority(request.getPriority());
        existing.setEstimatedHours(request.getEstimatedHours());
        existing.setRecurrenceRule(request.getRecurrenceRule());
        
        if (request.getSprintId() != null) {
            existing.setSprint(sprintRepository.findById(request.getSprintId())
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found")));
        } else {
            existing.setSprint(null);
        }

        if (request.getCompleted() != null) {
            existing.setCompleted(request.getCompleted());
        }

        AppUser owner = resolveOwnerForUpdate(existing, request);
        ensureProjectMember(owner, project);
        existing.setOwner(owner);

        if (column != null) {
            existing.setColumn(column);
            if (column.getStatus() == ColumnStatus.DONE) {
                existing.setCompleted(true);
            }
        }

        existing.setLabels(resolveLabels(request.getLabelIds(), project));
        
        Set<AppUser> newAssignees = resolveAssignees(request.getAssigneeIds(), project);
        Set<AppUser> currentAssignees = existing.getAssignees();
        existing.setAssignees(newAssignees);

        Task saved = taskRepository.save(existing);

        if (column != null) {
            activityService.log(saved, "Moved to " + column.getName(), "MOVE");
        }
        if (!previousOwner.getId().equals(owner.getId())) {
            activityService.log(saved, "Reassigned to " + owner.getDisplayName(), "ASSIGN");
            notificationService.create(owner, NotificationType.ASSIGNMENT, "You were assigned to " + saved.getTitle(), saved);
        }
        
        for (AppUser assignee : newAssignees) {
            if (!currentAssignees.contains(assignee)) {
                notificationService.create(assignee, NotificationType.ASSIGNMENT, "You were assigned to " + saved.getTitle(), saved);
            }
        }
        if (request.getCompleted() != null && request.getCompleted()) {
            notificationService.create(owner, NotificationType.COMPLETED, "Task completed: " + saved.getTitle(), saved);
        }

        return saved;
    }

    public Task toggleComplete(Long id, boolean completed) {
        Task existing = findById(id);
        ensureAccess(existing);
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

    public void delete(Long id) {
        Task existing = findById(id);
        ensureAccess(existing);
        taskRepository.delete(existing);
    }

    public Task addDependency(Long taskId, Long dependsOnId) {
        Task task = findById(taskId);
        Task dependsOn = findById(dependsOnId);
        ensureAccess(task);
        ensureAccess(dependsOn);

        if (task.getId().equals(dependsOn.getId())) {
            throw new IllegalArgumentException("Task cannot depend on itself");
        }

        task.getDependencies().add(dependsOn);
        Task saved = taskRepository.save(task);
        activityService.log(saved, "Added dependency on: " + dependsOn.getTitle(), "DEPENDENCY");
        return saved;
    }

    public Task removeDependency(Long taskId, Long dependsOnId) {
        Task task = findById(taskId);
        Task dependsOn = findById(dependsOnId);
        ensureAccess(task);

        task.getDependencies().remove(dependsOn);
        Task saved = taskRepository.save(task);
        activityService.log(saved, "Removed dependency on: " + dependsOn.getTitle(), "DEPENDENCY");
        return saved;
    }

    @Transactional
    public long deleteCompleted() {
        List<Long> taskIds;
        long deleted;
        if (securityService.isAdmin()) {
            taskIds = taskRepository.findIdsByCompletedTrue();
            if (!taskIds.isEmpty()) {
                notificationRepository.deleteByTaskIds(taskIds);
            }
            deleted = taskRepository.deleteByCompletedTrue();
        } else {
            Long ownerId = securityService.getCurrentUserId();
            taskIds = taskRepository.findIdsByCompletedTrueAndOwnerId(ownerId);
            if (!taskIds.isEmpty()) {
                notificationRepository.deleteByTaskIds(taskIds);
            }
            deleted = taskRepository.deleteByCompletedTrueAndOwnerId(ownerId);
        }
        return deleted;
    }

    private Task findById(Long id) {
        return taskRepository.findWithAssociationsById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
    }

    private void ensureAccess(Task task) {
        if (securityService.isAdmin()) {
            return;
        }
        Long userId = securityService.getCurrentUserId();
        if (task.getOwner() != null && task.getOwner().getId().equals(userId)) {
            return;
        }
        if (task.getAssignees().stream().anyMatch(u -> u.getId().equals(userId))) {
            return;
        }
        Project project = projectFromColumn(task.getColumn());
        projectService.ensureAccess(project);
    }

    private Specification<Task> applyOwnershipFilter(Specification<Task> spec, Long projectId) {
        if (!securityService.isAdmin() && projectId == null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("owner").get("id"), securityService.getCurrentUserId()));
        }
        return spec;
    }

    private AppUser resolveOwnerForCreate(Long requestedOwnerId) {
        Long currentUserId = securityService.getCurrentUserId();
        if (securityService.isAdmin()) {
            if (requestedOwnerId == null) {
                return appUserService.findById(currentUserId);
            }
            return appUserService.findById(requestedOwnerId);
        }
        if (requestedOwnerId != null && !requestedOwnerId.equals(currentUserId)) {
            throw new IllegalArgumentException("Users can only create tasks for themselves.");
        }
        return appUserService.findById(currentUserId);
    }

    private AppUser resolveOwnerForUpdate(Task existing, TaskRequest request) {
        if (securityService.isAdmin() && request.getOwnerId() != null && !request.getOwnerId().equals(existing.getOwner().getId())) {
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

    private Set<AppUser> resolveAssignees(List<Long> assigneeIds, Project project) {
        if (assigneeIds == null) {
            return new HashSet<>();
        }
        Set<AppUser> assignees = new HashSet<>();
        for (Long id : assigneeIds) {
            AppUser user = appUserService.findById(id);
            ensureProjectMember(user, project);
            assignees.add(user);
        }
        return assignees;
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

    private void notifyAssignment(Task task, AppUser owner) {
        Long userId = securityService.getCurrentUserId();
        if (owner.getId().equals(userId)) {
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