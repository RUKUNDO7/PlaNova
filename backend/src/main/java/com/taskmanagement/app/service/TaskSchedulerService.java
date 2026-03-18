package com.taskmanagement.app.service;

import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final ActivityService activityService;

    public TaskSchedulerService(TaskRepository taskRepository, ActivityService activityService) {
        this.taskRepository = taskRepository;
        this.activityService = activityService;
    }

    /**
     * Runs every day at midnight (system timezone) to check for recurring tasks
     * and generate the next iteration if needed.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringTasks() {
        // Simplified Logic: find tasks with recurrence rules that are marked complete, 
        // to spawn the next occurrence.
        // NOTE: A robust implementation would parse RRULE and check schedules.
        List<Task> recurringTasks = taskRepository.findAll().stream()
                .filter(t -> t.getRecurrenceRule() != null && !t.getRecurrenceRule().isBlank())
                .filter(Task::isCompleted)
                .toList();

        for (Task task : recurringTasks) {
            // For this implementation, we assume that if it's completed, we create the next one
            // and clear the recurrence rule on the OLD one so it doesn't keep spawning.
            
            Task nextTask = new Task();
            nextTask.setTitle(task.getTitle() + " (Recurring)");
            nextTask.setDescription(task.getDescription());
            nextTask.setPriority(task.getPriority());
            nextTask.setOwner(task.getOwner());
            nextTask.setColumn(task.getColumn());
            
            if (task.getDueDate() != null) {
                // simple example: assuming "DAILY" format for now to bump 1 day
                nextTask.setDueDate(task.getDueDate().plusDays(1));
            }

            nextTask.setRecurrenceRule(task.getRecurrenceRule());
            
            // clear rule on old task so we don't duplicate it again
            task.setRecurrenceRule(null);
            taskRepository.save(task);
            
            Task saved = taskRepository.save(nextTask);
            activityService.log(saved, "Automatically created from recurring sequence", "SYSTEM");
        }
    }
}
