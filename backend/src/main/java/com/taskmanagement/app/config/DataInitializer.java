package com.taskmanagement.app.config;

import com.taskmanagement.app.model.UserRole;
import com.taskmanagement.app.service.AppUserService;
import com.taskmanagement.app.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserService appUserService;
    private final AuthService authService;

    public DataInitializer(AppUserService appUserService, AuthService authService) {
        this.appUserService = appUserService;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        log.debug("Seeding user directory");
        authService.ensureAdminUser("Platform Admin", "AdminPass#1");
        appUserService.ensureSystemUser("ops@task.local", "Operations Lead", UserRole.USER, "OpsPass#1");
        appUserService.ensureSystemUser("marketing@task.local", "Marketing Specialist", UserRole.USER, "MarketPass#1");
    }
}
