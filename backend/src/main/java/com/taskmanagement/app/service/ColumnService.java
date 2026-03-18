package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.ColumnRequest;
import com.taskmanagement.app.model.Board;
import com.taskmanagement.app.model.BoardColumn;
import com.taskmanagement.app.model.ColumnStatus;
import com.taskmanagement.app.repository.BoardColumnRepository;
import com.taskmanagement.app.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final ProjectService projectService;

    public ColumnService(BoardColumnRepository columnRepository, BoardRepository boardRepository, ProjectService projectService) {
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
        this.projectService = projectService;
    }

    public List<BoardColumn> listByBoard(Long boardId) {
        Board board = findBoard(boardId);
        projectService.ensureAccess(board.getProject());
        return columnRepository.findByBoardIdOrderByPositionAsc(boardId);
    }

    public BoardColumn findById(Long id) {
        return columnRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Column not found with id " + id));
    }

    public BoardColumn create(ColumnRequest request) {
        if (request.getBoardId() == null) {
            throw new IllegalArgumentException("boardId is required.");
        }
        Board board = findBoard(request.getBoardId());
        projectService.ensureAccess(board.getProject());
        BoardColumn column = new BoardColumn();
        column.setName(request.getName());
        column.setBoard(board);
        column.setStatus(request.getStatus() == null ? ColumnStatus.TODO : request.getStatus());
        int position = request.getPosition() != null ? request.getPosition() : columnRepository.findByBoardIdOrderByPositionAsc(board.getId()).size();
        column.setPosition(position);
        return columnRepository.save(column);
    }

    public BoardColumn update(Long id, ColumnRequest request) {
        BoardColumn column = findById(id);
        projectService.ensureAccess(column.getBoard().getProject());
        column.setName(request.getName());
        if (request.getStatus() != null) {
            column.setStatus(request.getStatus());
        }
        if (request.getPosition() != null) {
            column.setPosition(request.getPosition());
        }
        return columnRepository.save(column);
    }

    public void delete(Long id) {
        BoardColumn column = findById(id);
        projectService.ensureAccess(column.getBoard().getProject());
        columnRepository.delete(column);
    }

    public BoardColumn findDoneColumn(Long boardId) {
        List<BoardColumn> columns = columnRepository.findByBoardIdAndStatus(boardId, ColumnStatus.DONE);
        return columns.isEmpty() ? null : columns.get(0);
    }

    public BoardColumn findTodoColumn(Long boardId) {
        List<BoardColumn> columns = columnRepository.findByBoardIdAndStatus(boardId, ColumnStatus.TODO);
        return columns.isEmpty() ? null : columns.get(0);
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
            .orElseThrow(() -> new EntityNotFoundException("Board not found with id " + boardId));
    }
}