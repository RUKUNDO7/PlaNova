package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.ColumnStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ColumnRequest {

    @NotBlank
    @Size(max = 80)
    private String name;

    private ColumnStatus status;

    private Integer position;

    private Long boardId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnStatus getStatus() {
        return status;
    }

    public void setStatus(ColumnStatus status) {
        this.status = status;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }
}