package com.taskmanagement.app.controller;

import com.taskmanagement.app.model.Webhook;
import com.taskmanagement.app.service.WebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/webhooks")
@CrossOrigin(origins = "http://localhost:5173")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping
    public List<Webhook> getWebhooks(@PathVariable Long projectId) {
        return webhookService.findByProjectId(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Webhook createWebhook(@PathVariable Long projectId,
                                 @RequestParam String name,
                                 @RequestParam String url,
                                 @RequestParam String events) {
        return webhookService.create(projectId, name, url, events);
    }

    @DeleteMapping("/{webhookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebhook(@PathVariable Long projectId,
                               @PathVariable Long webhookId) {
        webhookService.delete(webhookId);
    }
}
