package com.taskmanagement.app.service;

import com.taskmanagement.app.model.CustomFieldDefinition;
import com.taskmanagement.app.model.CustomFieldValue;
import com.taskmanagement.app.model.Project;
import com.taskmanagement.app.model.Task;
import com.taskmanagement.app.repository.CustomFieldDefinitionRepository;
import com.taskmanagement.app.repository.CustomFieldValueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomFieldService {

    private final CustomFieldDefinitionRepository definitionRepository;
    private final CustomFieldValueRepository valueRepository;
    private final ProjectService projectService;
    private final TaskService taskService;

    public CustomFieldService(CustomFieldDefinitionRepository definitionRepository,
                              CustomFieldValueRepository valueRepository,
                              ProjectService projectService,
                              TaskService taskService) {
        this.definitionRepository = definitionRepository;
        this.valueRepository = valueRepository;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    // Definitions
    public List<CustomFieldDefinition> findDefinitionsByProjectId(Long projectId) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project);
        return definitionRepository.findByProjectId(projectId);
    }

    public CustomFieldDefinition createDefinition(Long projectId, String label, CustomFieldDefinition.FieldType type, boolean required) {
        Project project = projectService.findById(projectId);
        projectService.ensureAccess(project);
        
        CustomFieldDefinition def = new CustomFieldDefinition();
        def.setProject(project);
        def.setLabel(label);
        def.setFieldType(type);
        def.setRequired(required);
        return definitionRepository.save(def);
    }

    public void deleteDefinition(Long definitionId) {
        CustomFieldDefinition def = definitionRepository.findById(definitionId)
            .orElseThrow(() -> new EntityNotFoundException("Field definition not found"));
        projectService.ensureAccess(def.getProject());
        definitionRepository.delete(def);
    }

    // Values
    public List<CustomFieldValue> findValuesByTaskId(Long taskId) {
        taskService.findAccessibleById(taskId);
        return valueRepository.findByTaskId(taskId);
    }

    public CustomFieldValue setFieldValue(Long taskId, Long definitionId, String value) {
        Task task = taskService.findAccessibleById(taskId);
        CustomFieldDefinition def = definitionRepository.findById(definitionId)
            .orElseThrow(() -> new EntityNotFoundException("Field definition not found"));
            
        if (!def.getProject().getId().equals(task.getColumn().getBoard().getProject().getId())) {
            throw new IllegalArgumentException("Custom field does not belong to the task's project");
        }

        Optional<CustomFieldValue> existingOpt = valueRepository.findByTaskIdAndDefinitionId(taskId, definitionId);
        CustomFieldValue fieldValue = existingOpt.orElseGet(() -> {
            CustomFieldValue newValue = new CustomFieldValue();
            newValue.setTask(task);
            newValue.setDefinition(def);
            return newValue;
        });
        
        fieldValue.setValue(value);
        return valueRepository.save(fieldValue);
    }
}
