package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.SprintRequest;
import com.taskmanagement.app.dto.SprintResponse;
import com.taskmanagement.app.model.Sprint;
import com.taskmanagement.app.service.SprintService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards/{boardId}/sprints")
@CrossOrigin(origins = "http://localhost:5173")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping
    public List<SprintResponse> getSprints(@PathVariable Long boardId) {
        return sprintService.findByBoardId(boardId).stream()
            .map(SprintResponse::from).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse createSprint(@PathVariable Long boardId, @Valid @RequestBody SprintRequest request) {
        Sprint s = sprintService.create(boardId, request.getName(), request.getStartDate(), request.getEndDate());
        return SprintResponse.from(s);
    }

    @PutMapping("/{sprintId}")
    public SprintResponse updateSprint(@PathVariable Long boardId, @PathVariable Long sprintId, @Valid @RequestBody SprintRequest request) {
        Sprint s = sprintService.update(sprintId, request.getName(), request.getStartDate(), request.getEndDate(), request.getStatus());
        return SprintResponse.from(s);
    }
}
