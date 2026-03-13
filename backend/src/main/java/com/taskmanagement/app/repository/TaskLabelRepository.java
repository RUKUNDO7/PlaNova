package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.TaskLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {
    List<TaskLabel> findByProjectIdOrderByNameAsc(Long projectId);
}