package com.inhubber.identity.controller;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.dto.LoginRequest;
import com.inhubber.identity.dto.RegisterRequest;
import com.inhubber.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(LoginRequest request) {
        Boolean success = authService.login(request.email(), request.password());
        return ResponseEntity.ok(success);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(RegisterRequest request) {
        authService.register(request.email(), request.password(), Role.USER);
        return ResponseEntity.ok().build();
    }
}
