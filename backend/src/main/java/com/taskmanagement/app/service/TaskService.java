package com.taskmanagement.app.service;

import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.Priority;
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

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll(String status, Priority priority, String q, String sortBy, String direction) {
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

        Sort sort = buildSort(sortBy, direction);
        return taskRepository.findAll(spec, sort);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
    }

    public Task create(Task task) {
        task.setId(null);
        return taskRepository.save(task);
    }

    public Task update(Long id, Task payload) {
        Task existing = findById(id);
        existing.setTitle(payload.getTitle());
        existing.setDescription(payload.getDescription());
        existing.setDueDate(payload.getDueDate());
        existing.setPriority(payload.getPriority());
        existing.setCompleted(payload.isCompleted());
        return taskRepository.save(existing);
    }

    public Task toggleComplete(Long id, boolean completed) {
        Task existing = findById(id);
        existing.setCompleted(completed);
        return taskRepository.save(existing);
    }

    public void delete(Long id) {
        Task existing = findById(id);
        taskRepository.delete(existing);
    }

    public long deleteCompleted() {
        return taskRepository.deleteByCompletedTrue();
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
