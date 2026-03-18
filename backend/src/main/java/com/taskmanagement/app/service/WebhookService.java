package com.taskmanagement.app.service;

import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.Webhook;
import com.taskmanagement.app.repository.WebhookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final ProjectService projectService;

    public WebhookService(WebhookRepository webhookRepository, ProjectService projectService) {
        this.webhookRepository = webhookRepository;
        this.projectService = projectService;
    }

    public List<Webhook> findByProjectId(Long projectId) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project);
        return webhookRepository.findByProjectIdAndActiveTrue(projectId);
    }

    public Webhook create(Long projectId, String name, String url, String events) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project);
        
        Webhook webhook = new Webhook();
        webhook.setProject(project);
        webhook.setName(name);
        webhook.setUrl(url);
        webhook.setEvents(events);
        webhook.setActive(true);
        return webhookRepository.save(webhook);
    }

    public Webhook update(Long id, String name, String url, String events, Boolean active) {
        Webhook existing = webhookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Webhook not found"));
        projectService.ensureAccess(existing.getProject());
        
        if (name != null) existing.setName(name);
        if (url != null) existing.setUrl(url);
        if (events != null) existing.setEvents(events);
        if (active != null) existing.setActive(active);
        
        return webhookRepository.save(existing);
    }

    public void delete(Long id) {
        Webhook existing = webhookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Webhook not found"));
        projectService.ensureAccess(existing.getProject());
        webhookRepository.delete(existing);
    }
}
