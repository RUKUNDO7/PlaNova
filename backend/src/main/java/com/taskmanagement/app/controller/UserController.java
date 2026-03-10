package com.taskmanagement.app.controller;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.service.AppUserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<UserSummary> list() {
        return appUserService.findAll().stream()
            .map(UserSummary::from)
            .collect(Collectors.toList());
    }

    public record UserSummary(Long id, String email, String displayName, UserRole role) {
        public static UserSummary from(AppUser user) {
            return new UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
        }
    }
}
