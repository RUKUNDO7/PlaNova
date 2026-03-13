package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.BoardRequest;
import com.taskmanagement.app.model.Board;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final ProjectService projectService;

    public BoardService(BoardRepository boardRepository, ProjectService projectService) {
        this.boardRepository = boardRepository;
        this.projectService = projectService;
    }

    public List<Board> listByProject(Long projectId, UserRole viewerRole, Long viewerId) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project, viewerRole, viewerId);
        return boardRepository.findByProjectIdOrderByPositionAsc(projectId);
    }

    public Board findById(Long id) {
        return boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Board not found with id " + id));
    }

    public Board create(BoardRequest request, UserRole viewerRole, Long viewerId) {
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("projectId is required.");
        }
        Project project = projectService.findById(request.getProjectId());
        projectService.ensureAccess(project, viewerRole, viewerId);
        Board board = new Board();
        board.setName(request.getName());
        board.setProject(project);
        int position = request.getPosition() != null ? request.getPosition() : boardRepository.findByProjectIdOrderByPositionAsc(project.getId()).size();
        board.setPosition(position);
        return boardRepository.save(board);
    }

    public Board update(Long id, BoardRequest request, UserRole viewerRole, Long viewerId) {
        Board board = findById(id);
        projectService.ensureAccess(board.getProject(), viewerRole, viewerId);
        board.setName(request.getName());
        if (request.getPosition() != null) {
            board.setPosition(request.getPosition());
        }
        return boardRepository.save(board);
    }

    public void delete(Long id, UserRole viewerRole, Long viewerId) {
        Board board = findById(id);
        projectService.ensureAccess(board.getProject(), viewerRole, viewerId);
        boardRepository.delete(board);
    }
}