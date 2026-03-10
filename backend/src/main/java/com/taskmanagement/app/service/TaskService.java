package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.TaskRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Priority;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserService appUserService;

    public TaskService(TaskRepository taskRepository, AppUserService appUserService) {
        this.taskRepository = taskRepository;
        this.appUserService = appUserService;
    }

    public List<Task> findAll(String status, Priority priority, String q, String sortBy, String direction, UserRole viewerRole, Long viewerId) {
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

        spec = applyOwnershipFilter(spec, viewerRole, viewerId);

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
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setCompleted(Boolean.TRUE.equals(request.getCompleted()));
        task.setOwner(owner);
        return taskRepository.save(task);
    }

    public Task update(Long id, TaskRequest request, UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        Task existing = findById(id);
        ensureAccess(existing, viewerRole, viewerId);
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setDueDate(request.getDueDate());
        existing.setPriority(request.getPriority());
        if (request.getCompleted() != null) {
            existing.setCompleted(request.getCompleted());
        }
        AppUser owner = resolveOwnerForUpdate(existing, request, viewerRole);
        existing.setOwner(owner);
        return taskRepository.save(existing);
    }

    public Task toggleComplete(Long id, boolean completed, UserRole viewerRole, Long viewerId) {
        ensureViewerContext(viewerRole, viewerId);
        Task existing = findById(id);
        ensureAccess(existing, viewerRole, viewerId);
        existing.setCompleted(completed);
        return taskRepository.save(existing);
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
        if (viewerRole == UserRole.USER && !task.getOwner().getId().equals(viewerId)) {
            throw new IllegalArgumentException("Users can only access their own tasks.");
        }
    }

    private Specification<Task> applyOwnershipFilter(Specification<Task> spec, UserRole viewerRole, Long viewerId) {
        if (viewerRole == UserRole.USER) {
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

    private Sort buildSort(String sortBy, String direction) {
        String normalizedSortBy = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        String normalizedDirection = StringUtils.hasText(direction) ? direction : "desc";

        List<String> allowedFields = List.of("createdAt", "dueDate", "priority", "title");
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
