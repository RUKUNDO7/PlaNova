package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByBoardId(Long boardId);
}
