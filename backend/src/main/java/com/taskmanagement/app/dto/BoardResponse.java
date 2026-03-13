package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.Board;

public class BoardResponse {

    private Long id;
    private String name;
    private int position;
    private Long projectId;

    public static BoardResponse from(Board board) {
        BoardResponse response = new BoardResponse();
        response.id = board.getId();
        response.name = board.getName();
        response.position = board.getPosition();
        response.projectId = board.getProject().getId();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public Long getProjectId() {
        return projectId;
    }
}