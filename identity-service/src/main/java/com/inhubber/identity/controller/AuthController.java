package com.inhubber.identity.controller;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.dto.LoginRequest;
import com.inhubber.identity.dto.LoginResponse;
import com.inhubber.identity.dto.RegisterRequest;
import com.inhubber.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request.email(), request.password(), Role.USER);
        return ResponseEntity.ok().build();
    }
}
