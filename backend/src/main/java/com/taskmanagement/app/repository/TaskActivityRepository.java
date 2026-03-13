package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}