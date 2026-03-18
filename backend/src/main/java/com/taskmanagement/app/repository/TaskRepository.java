package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    long deleteByCompletedTrue();
    long deleteByCompletedTrueAndOwnerId(Long ownerId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.owner.id = :ownerId AND t.completed = false")
    long countActiveTasksByOwnerId(@Param("ownerId") Long ownerId);
    
    long countByCompletedTrue();
    List<Task> findByColumnIsNull();
}