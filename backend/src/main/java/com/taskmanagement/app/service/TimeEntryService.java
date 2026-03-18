package com.taskmanagement.app.service;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.model.TimeEntry;
import com.taskmanagement.app.security.SecurityService;
import com.taskmanagement.app.repository.TimeEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskService taskService;
    private final AppUserService appUserService;
    private final ActivityService activityService;
    private final SecurityService securityService;

    public TimeEntryService(TimeEntryRepository timeEntryRepository, 
                            TaskService taskService, 
                            AppUserService appUserService, 
                            ActivityService activityService,
                            SecurityService securityService) {
        this.timeEntryRepository = timeEntryRepository;
        this.taskService = taskService;
        this.appUserService = appUserService;
        this.activityService = activityService;
        this.securityService = securityService;
    }

    public List<TimeEntry> findByTaskId(Long taskId) {
        taskService.findAccessibleById(taskId);
        return timeEntryRepository.findByTaskId(taskId);
    }

    public TimeEntry create(Long taskId, Double hours, String description, LocalDate date) {
        Task task = taskService.findAccessibleById(taskId);
        AppUser user = appUserService.findById(securityService.getCurrentUserId());
        
        TimeEntry entry = new TimeEntry();
        entry.setTask(task);
        entry.setUser(user);
        entry.setHours(hours);
        entry.setDescription(description);
        entry.setDate(date != null ? date : LocalDate.now());
        
        TimeEntry saved = timeEntryRepository.save(entry);
        activityService.log(task, "Logged " + hours + " hours", "TIME");
        return saved;
    }

    public void delete(Long id) {
        TimeEntry existing = timeEntryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("TimeEntry not found"));
            
        if (!securityService.isAdmin() && !existing.getUser().getId().equals(securityService.getCurrentUserId())) {
            throw new IllegalArgumentException("Cannot delete someone else's time entry");
        }
        
        timeEntryRepository.delete(existing);
        activityService.log(existing.getTask(), "Removed " + existing.getHours() + " logged hours", "TIME");
    }
}
