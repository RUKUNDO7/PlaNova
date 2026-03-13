package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.BoardRequest;
import com.taskmanagement.app.dto.BoardResponse;
import com.taskmanagement.app.model.Board;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
@CrossOrigin(origins = "http://localhost:5173")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<BoardResponse> list(
        @RequestParam Long projectId,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        return boardService.listByProject(projectId, viewerRole, viewerId).stream()
            .map(BoardResponse::from)
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(
        @Valid @RequestBody BoardRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Board created = boardService.create(request, viewerRole, viewerId);
        return BoardResponse.from(created);
    }

    @PutMapping("/{id}")
    public BoardResponse update(
        @PathVariable Long id,
        @Valid @RequestBody BoardRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        Board updated = boardService.update(id, request, viewerRole, viewerId);
        return BoardResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        boardService.delete(id, viewerRole, viewerId);
    }
}