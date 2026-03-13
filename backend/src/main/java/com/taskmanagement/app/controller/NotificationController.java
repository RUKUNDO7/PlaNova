package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.NotificationResponse;
import com.taskmanagement.app.model.Notification;
import com.taskmanagement.app.service.NotificationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestParam Long recipientId) {
        return notificationService.list(recipientId).stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id, @RequestParam Long recipientId) {
        Notification updated = notificationService.markRead(id, recipientId);
        return NotificationResponse.from(updated);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllRead(@RequestParam Long recipientId) {
        int updated = notificationService.markAllRead(recipientId);
        return Map.of("updated", updated);
    }
}