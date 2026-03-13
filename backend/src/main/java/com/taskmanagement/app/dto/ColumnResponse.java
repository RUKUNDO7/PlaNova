package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.BoardColumn;

public class ColumnResponse {

    private Long id;
    private String name;
    private String status;
    private int position;
    private Long boardId;

    public static ColumnResponse from(BoardColumn column) {
        ColumnResponse response = new ColumnResponse();
        response.id = column.getId();
        response.name = column.getName();
        response.status = column.getStatus().name();
        response.position = column.getPosition();
        response.boardId = column.getBoard().getId();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getPosition() {
        return position;
    }

    public Long getBoardId() {
        return boardId;
    }
}