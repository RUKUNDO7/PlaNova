package com.taskmanagement.app.security;

import com.taskmanagement.app.model.AppUser;
import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    private final AppUserRepository userRepository;

    public SecurityService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().map(AppUser::getId).orElse(null);
    }

    public UserRole getCurrentUserRole() {
        return getCurrentUser().map(AppUser::getRole).orElse(null);
    }

    public boolean isAdmin() {
        return getCurrentUserRole() == UserRole.ADMIN;
    }
}
