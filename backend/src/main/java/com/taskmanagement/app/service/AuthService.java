package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.AuthResponse;
import com.taskmanagement.app.dto.LoginRequest;
import com.taskmanagement.app.dto.SignupRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.UserRole;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;

    public AuthService(AppUserService appUserService,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email:gihozoRukundobenise@gmail.com}") String adminEmail) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
    }

    public AuthResponse signup(SignupRequest request) {
        String normalizedEmail = normalize(request.getEmail());
        UserRole role = isAdminEmail(normalizedEmail) ? UserRole.ADMIN : UserRole.USER;
        AppUser user = appUserService.register(normalizedEmail, request.getDisplayName(), request.getPassword(), role);
        return AuthResponse.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalize(request.getEmail());
        AppUser stored = appUserService.findByEmail(normalizedEmail);
        if (!passwordEncoder.matches(request.getPassword(), stored.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        return AuthResponse.from(stored);
    }

    public AppUser ensureAdminUser(String displayName, String password) {
        return appUserService.ensureSystemUser(adminEmail, displayName, UserRole.ADMIN, password);
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAdminEmail(String email) {
        return adminEmail.equalsIgnoreCase(email);
    }
}
