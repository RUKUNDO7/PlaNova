package com.taskmanagement.app.dto;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.UserRole;

public class AuthResponse {

    private Long id;
    private String email;
    private String displayName;
    private UserRole role;
    private String token;
    private String type = "Bearer";

    public static AuthResponse from(AppUser user, String token) {
        AuthResponse response = new AuthResponse();
        response.id = user.getId();
        response.email = user.getEmail();
        response.displayName = user.getDisplayName();
        response.role = user.getRole();
        response.token = token;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }
}
