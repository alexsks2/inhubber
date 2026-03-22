package com.inhubber.identity.config;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AuthService authService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!authService.existsByEmail(adminEmail)) {
            authService.register(adminEmail, adminPassword, Role.ADMIN);
        }
    }
}
