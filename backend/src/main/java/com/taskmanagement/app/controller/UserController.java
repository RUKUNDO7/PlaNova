package com.taskmanagement.app.controller;

import com.taskmanagement.app.dto.ProfileUpdateRequest;
import com.taskmanagement.app.dto.UserSummaryResponse;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<UserSummaryResponse> list() {
        return appUserService.findAll().stream()
            .map(UserSummaryResponse::from)
            .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserSummaryResponse updateProfile(@PathVariable Long id, @Valid @RequestBody ProfileUpdateRequest request) {
        AppUser updated = appUserService.updateDisplayName(id, request.getDisplayName());
        return UserSummaryResponse.from(updated);
    }
}