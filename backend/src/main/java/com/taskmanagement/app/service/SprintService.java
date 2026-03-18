package com.taskmanagement.app.service;

import com.taskmanagement.app.model.Board;
import com.taskmanagement.app.model.Sprint;
import com.taskmanagement.app.repository.BoardRepository;
import com.taskmanagement.app.repository.SprintRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final BoardRepository boardRepository;
    private final ProjectService projectService;

    public SprintService(SprintRepository sprintRepository, BoardRepository boardRepository, ProjectService projectService) {
        this.sprintRepository = sprintRepository;
        this.boardRepository = boardRepository;
        this.projectService = projectService;
    }

    public List<Sprint> findByBoardId(Long boardId) {
        Board board = getBoardAndEnsureAccess(boardId);
        return sprintRepository.findByBoardId(board.getId());
    }

    public Sprint create(Long boardId, String name, LocalDate startDate, LocalDate endDate) {
        Board board = getBoardAndEnsureAccess(boardId);
        Sprint sprint = new Sprint();
        sprint.setBoard(board);
        sprint.setName(name);
        sprint.setStartDate(startDate);
        sprint.setEndDate(endDate);
        sprint.setStatus(Sprint.Status.PLANNING);
        return sprintRepository.save(sprint);
    }
    
    public Sprint update(Long id, String name, LocalDate startDate, LocalDate endDate, Sprint.Status status) {
        Sprint existing = sprintRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sprint not found"));
        getBoardAndEnsureAccess(existing.getBoard().getId());
        
        if (name != null) existing.setName(name);
        if (startDate != null) existing.setStartDate(startDate);
        if (endDate != null) existing.setEndDate(endDate);
        if (status != null) existing.setStatus(status);
        
        return sprintRepository.save(existing);
    }

    private Board getBoardAndEnsureAccess(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new EntityNotFoundException("Board not found"));
        projectService.ensureAccess(board.getProject());
        return board;
    }
}
