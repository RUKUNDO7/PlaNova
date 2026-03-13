package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.BoardColumn;
import com.taskmanagement.app.model.ColumnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByBoardIdOrderByPositionAsc(Long boardId);
    boolean existsByBoardIdAndStatus(Long boardId, ColumnStatus status);
    List<BoardColumn> findByBoardIdAndStatus(Long boardId, ColumnStatus status);
}