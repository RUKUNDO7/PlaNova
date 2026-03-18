package com.taskmanagement.app.controller;

import com.taskmanagement.app.model.Template;
import com.taskmanagement.app.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "http://localhost:5173")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<Template> getTemplates() {
        return templateService.findAccessibleTemplates();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Template createTemplate(@RequestParam String name,
                                   @RequestParam(required = false) String description,
                                   @RequestParam Template.TemplateType type,
                                   @RequestBody String structure) {
        return templateService.create(name, description, type, structure);
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable Long templateId) {
        templateService.delete(templateId);
    }
}
