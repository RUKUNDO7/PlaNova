package com.taskmanagement.app.service;

import com.taskmanagement.app.model.Subtask;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.repository.SubtaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskService taskService;
    private final ActivityService activityService;

    public SubtaskService(SubtaskRepository subtaskRepository, TaskService taskService, ActivityService activityService) {
        this.subtaskRepository = subtaskRepository;
        this.taskService = taskService;
        this.activityService = activityService;
    }

    public List<Subtask> findByTaskId(Long taskId) {
        taskService.findAccessibleById(taskId); // ensure access
        return subtaskRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    public Subtask create(Long taskId, String title) {
        Task task = taskService.findAccessibleById(taskId);
        
        Subtask subtask = new Subtask();
        subtask.setTask(task);
        subtask.setTitle(title);
        subtask.setCompleted(false);
        
        Subtask saved = subtaskRepository.save(subtask);
        activityService.log(task, "Added subtask: " + title, "SUBTASK");
        return saved;
    }

    public Subtask update(Long id, String title, Boolean completed) {
        Subtask existing = findById(id);
        taskService.findAccessibleById(existing.getTask().getId()); // verify
        
        if (title != null) {
            existing.setTitle(title);
        }
        if (completed != null) {
            existing.setCompleted(completed);
            activityService.log(existing.getTask(), "Marked subtask '" + existing.getTitle() + "' " + (completed ? "complete" : "active"), "SUBTASK");
        }
        
        return subtaskRepository.save(existing);
    }

    public void delete(Long id) {
        Subtask existing = findById(id);
        taskService.findAccessibleById(existing.getTask().getId()); // verify
        
        subtaskRepository.delete(existing);
        activityService.log(existing.getTask(), "Removed subtask: " + existing.getTitle(), "SUBTASK");
    }

    private Subtask findById(Long id) {
        return subtaskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Subtask not found with id " + id));
    }
}
