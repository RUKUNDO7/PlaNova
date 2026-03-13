package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByProjectIdOrderByPositionAsc(Long projectId);
}