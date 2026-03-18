package com.taskmanagement.app.dto;

public class CustomFieldValueRequest {
    private Long definitionId;
    private String value;

    public Long getDefinitionId() { return definitionId; }
    public void setDefinitionId(Long definitionId) { this.definitionId = definitionId; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
