package com.taskmanagement.app.service;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AppUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    public AppUser findByEmail(String email) {
        return userRepository.findByEmail(normalize(email))
            .orElseThrow(() -> new EntityNotFoundException("User not found with email " + email));
    }

    public List<AppUser> findAll() {
        return userRepository.findAll(Sort.by("role").and(Sort.by("displayName")));
    }

    public AppUser register(String email, String displayName, String rawPassword, UserRole role) {
        String normalized = normalize(email);
        if (userRepository.findByEmail(normalized).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + normalized);
        }
        AppUser user = new AppUser();
        user.setEmail(normalized);
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public AppUser ensureSystemUser(String email, String displayName, UserRole role, String rawPassword) {
        String normalized = normalize(email);
        return userRepository.findByEmail(normalized)
            .map(existing -> {
                existing.setDisplayName(displayName);
                existing.setRole(role);
                if (rawPassword != null && !rawPassword.isBlank()) {
                    existing.setPasswordHash(passwordEncoder.encode(rawPassword));
                }
                return userRepository.save(existing);
            })
            .orElseGet(() -> register(normalized, displayName, rawPassword, role));
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
