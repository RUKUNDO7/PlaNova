package com.taskmanagement.app.service;

import com.taskmanagement.app.dto.AuthResponse;
import com.taskmanagement.app.dto.LoginRequest;
import com.taskmanagement.app.dto.SignupRequest;
import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserService appUserService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final String adminEmail;

    public AuthService(AppUserService appUserService,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       @Value("${app.admin.email}") String adminEmail) {
        this.appUserService = appUserService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.adminEmail = adminEmail;
    }

    public AuthResponse signup(SignupRequest request) {
        String normalizedEmail = normalize(request.getEmail());
        UserRole role = isAdminEmail(normalizedEmail) ? UserRole.ADMIN : UserRole.USER;
        AppUser user = appUserService.register(normalizedEmail, request.getDisplayName(), request.getPassword(), role);
        
        // Auto-login after signup to get token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return AuthResponse.from(user, jwt);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalize(request.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        AppUser user = appUserService.findByEmail(normalizedEmail);
        return AuthResponse.from(user, jwt);
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
