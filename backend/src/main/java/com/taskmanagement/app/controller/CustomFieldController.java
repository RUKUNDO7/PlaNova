package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.CustomFieldDefinitionResponse;
import com.taskmanagement.app.dto.CustomFieldValueRequest;
import com.taskmanagement.app.dto.CustomFieldValueResponse;
import com.taskmanagement.app.model.CustomFieldDefinition;
import com.taskmanagement.app.model.CustomFieldValue;
import com.taskmanagement.app.service.CustomFieldService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    public CustomFieldController(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    // Definitions
    @GetMapping("/projects/{projectId}/custom-fields")
    public List<CustomFieldDefinitionResponse> getDefinitions(@PathVariable Long projectId) {
        return customFieldService.findDefinitionsByProjectId(projectId).stream()
            .map(CustomFieldDefinitionResponse::from).collect(Collectors.toList());
    }

    @PostMapping("/projects/{projectId}/custom-fields")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomFieldDefinitionResponse createDefinition(@PathVariable Long projectId,
                                                          @RequestParam String label,
                                                          @RequestParam CustomFieldDefinition.FieldType type,
                                                          @RequestParam(defaultValue = "false") boolean required) {
        CustomFieldDefinition d = customFieldService.createDefinition(projectId, label, type, required);
        return CustomFieldDefinitionResponse.from(d);
    }

    @DeleteMapping("/custom-fields/definitions/{definitionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDefinition(@PathVariable Long definitionId) {
        customFieldService.deleteDefinition(definitionId);
    }

    // Values
    @GetMapping("/tasks/{taskId}/custom-fields")
    public List<CustomFieldValueResponse> getValues(@PathVariable Long taskId) {
        return customFieldService.findValuesByTaskId(taskId).stream()
            .map(CustomFieldValueResponse::from).collect(Collectors.toList());
    }

    @PostMapping("/tasks/{taskId}/custom-fields")
    @ResponseStatus(HttpStatus.OK)
    public CustomFieldValueResponse setFieldValue(@PathVariable Long taskId,
                                                  @Valid @RequestBody CustomFieldValueRequest request) {
        CustomFieldValue v = customFieldService.setFieldValue(taskId, request.getDefinitionId(), request.getValue());
        return CustomFieldValueResponse.from(v);
    }
}
