package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
