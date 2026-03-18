package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByTaskId(Long taskId);
    List<TimeEntry> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t WHERE t.task.id = :taskId")
    Double sumHoursByTaskId(Long taskId);
}
