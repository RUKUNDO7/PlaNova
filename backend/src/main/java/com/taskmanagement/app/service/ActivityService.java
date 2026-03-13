package com.taskmanagement.app.service;

import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.TaskActivity;
import com.taskmanagement.app.repository.TaskActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final TaskActivityRepository activityRepository;

    public ActivityService(TaskActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public TaskActivity log(Task task, String message, String type) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setMessage(message);
        activity.setType(type);
        return activityRepository.save(activity);
    }

    public List<TaskActivity> listForTask(Long taskId) {
        return activityRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
}