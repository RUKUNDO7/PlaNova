package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.ColumnRequest;
import com.taskmanagement.app.dto.ColumnResponse;
import com.taskmanagement.app.model.BoardColumn;
import com.taskmanagement.app.service.ColumnService;
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
@RequestMapping("/api/columns")
@CrossOrigin(origins = "http://localhost:5173")
public class ColumnController {

    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @GetMapping
    public List<ColumnResponse> list(@RequestParam Long boardId) {
        return columnService.listByBoard(boardId).stream()
            .map(ColumnResponse::from)
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ColumnResponse create(@Valid @RequestBody ColumnRequest request) {
        BoardColumn created = columnService.create(request);
        return ColumnResponse.from(created);
    }

    @PutMapping("/{id}")
    public ColumnResponse update(@PathVariable Long id, @Valid @RequestBody ColumnRequest request) {
        BoardColumn updated = columnService.update(id, request);
        return ColumnResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        columnService.delete(id);
    }
}