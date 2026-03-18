package com.taskmanagement.app.service;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.Template;
import com.taskmanagement.app.security.SecurityService;
import com.taskmanagement.app.repository.TemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final AppUserService appUserService;
    private final SecurityService securityService;

    public TemplateService(TemplateRepository templateRepository, AppUserService appUserService, SecurityService securityService) {
        this.templateRepository = templateRepository;
        this.appUserService = appUserService;
        this.securityService = securityService;
    }

    public List<Template> findAccessibleTemplates() {
        if (securityService.isAdmin()) {
            return templateRepository.findAll();
        }
        return templateRepository.findByCreatedById(securityService.getCurrentUserId());
    }

    public Template create(String name, String description, Template.TemplateType type, String structure) {
        AppUser user = appUserService.findById(securityService.getCurrentUserId());
        Template template = new Template();
        template.setName(name);
        template.setDescription(description);
        template.setType(type != null ? type : Template.TemplateType.PROJECT);
        template.setStructure(structure);
        template.setCreatedBy(user);
        return templateRepository.save(template);
    }

    public void delete(Long id) {
        Template existing = templateRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Template not found"));
        if (!securityService.isAdmin() && !existing.getCreatedBy().getId().equals(securityService.getCurrentUserId())) {
            throw new IllegalArgumentException("Can only delete your own templates");
        }
        templateRepository.delete(existing);
    }
}
