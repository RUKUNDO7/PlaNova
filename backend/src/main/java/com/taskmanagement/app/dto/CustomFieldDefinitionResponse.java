package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.CustomFieldDefinition;

public class CustomFieldDefinitionResponse {
    private Long id;
    private String label;
    private CustomFieldDefinition.FieldType fieldType;
    private boolean required;

    public static CustomFieldDefinitionResponse from(CustomFieldDefinition d) {
        CustomFieldDefinitionResponse r = new CustomFieldDefinitionResponse();
        r.id = d.getId();
        r.label = d.getLabel();
        r.fieldType = d.getFieldType();
        r.required = d.isRequired();
        return r;
    }

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public CustomFieldDefinition.FieldType getFieldType() { return fieldType; }
    public boolean isRequired() { return required; }
}
