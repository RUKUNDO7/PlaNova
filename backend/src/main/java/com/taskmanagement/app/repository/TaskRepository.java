package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    long deleteByCompletedTrue();
    long deleteByCompletedTrueAndOwnerId(Long ownerId);
    List<Task> findByColumnIsNull();
}