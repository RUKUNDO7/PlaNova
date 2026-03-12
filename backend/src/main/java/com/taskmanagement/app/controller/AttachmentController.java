package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.AttachmentRequest;
import com.taskmanagement.app.dto.AttachmentResponse;
import com.taskmanagement.app.model.TaskAttachment;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.service.AttachmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
@CrossOrigin(origins = "http://localhost:5173")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<AttachmentResponse> list(
        @PathVariable Long taskId,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        return attachmentService.list(taskId, viewerRole, viewerId).stream()
            .map(AttachmentResponse::from)
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse create(
        @PathVariable Long taskId,
        @Valid @RequestBody AttachmentRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        TaskAttachment created = attachmentService.create(taskId, request, viewerRole, viewerId);
        return AttachmentResponse.from(created);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable Long taskId,
        @PathVariable Long attachmentId,
        @RequestParam(required = false, defaultValue = "ADMIN") UserRole viewerRole,
        @RequestParam(required = false) Long viewerId
    ) {
        attachmentService.delete(taskId, attachmentId, viewerRole, viewerId);
    }
}