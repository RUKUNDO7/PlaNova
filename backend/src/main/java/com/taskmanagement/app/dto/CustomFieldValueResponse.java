package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.CustomFieldValue;

public class CustomFieldValueResponse {
    private Long id;
    private Long definitionId;
    private String label;
    private String value;

    public static CustomFieldValueResponse from(CustomFieldValue v) {
        CustomFieldValueResponse r = new CustomFieldValueResponse();
        r.id = v.getId();
        r.definitionId = v.getDefinition().getId();
        r.label = v.getDefinition().getLabel();
        r.value = v.getValue();
        return r;
    }

    public Long getId() { return id; }
    public Long getDefinitionId() { return definitionId; }
    public String getLabel() { return label; }
    public String getValue() { return value; }
}
