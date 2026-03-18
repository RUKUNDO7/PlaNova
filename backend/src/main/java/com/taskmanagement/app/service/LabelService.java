package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.LabelRequest;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.TaskLabel;
import com.taskmanagement.app.repository.TaskLabelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService {

    private final TaskLabelRepository labelRepository;
    private final ProjectService projectService;

    public LabelService(TaskLabelRepository labelRepository, ProjectService projectService) {
        this.labelRepository = labelRepository;
        this.projectService = projectService;
    }

    public List<TaskLabel> listByProject(Long projectId) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project);
        return labelRepository.findByProjectIdOrderByNameAsc(projectId);
    }

    public TaskLabel findById(Long id) {
        return labelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Label not found with id " + id));
    }

    public TaskLabel create(LabelRequest request) {
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("projectId is required.");
        }
        Project project = projectService.findById(request.getProjectId());
        projectService.ensureAccess(project);
        TaskLabel label = new TaskLabel();
        label.setName(request.getName());
        label.setColor(normalizeColor(request.getColor()));
        label.setProject(project);
        return labelRepository.save(label);
    }

    public TaskLabel update(Long id, LabelRequest request) {
        TaskLabel label = findById(id);
        projectService.ensureAccess(label.getProject());
        label.setName(request.getName());
        label.setColor(normalizeColor(request.getColor()));
        return labelRepository.save(label);
    }

    public void delete(Long id) {
        TaskLabel label = findById(id);
        projectService.ensureAccess(label.getProject());
        labelRepository.delete(label);
    }

    private String normalizeColor(String color) {
        if (color == null) {
            return "#94a3b8";
        }
        String trimmed = color.trim();
        return trimmed.startsWith("#") ? trimmed : "#" + trimmed;
    }
}