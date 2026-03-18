package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.LabelRequest;
import com.taskmanagement.app.dto.LabelResponse;
import com.taskmanagement.app.model.TaskLabel;
import com.taskmanagement.app.service.LabelService;
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
@RequestMapping("/api/labels")
@CrossOrigin(origins = "http://localhost:5173")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public List<LabelResponse> list(@RequestParam Long projectId) {
        return labelService.listByProject(projectId).stream()
            .map(LabelResponse::from)
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse create(@Valid @RequestBody LabelRequest request) {
        TaskLabel created = labelService.create(request);
        return LabelResponse.from(created);
    }

    @PutMapping("/{id}")
    public LabelResponse update(@PathVariable Long id, @Valid @RequestBody LabelRequest request) {
        TaskLabel updated = labelService.update(id, request);
        return LabelResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        labelService.delete(id);
    }
}