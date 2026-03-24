package com.inhubber.identity.controller;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.dto.LoginRequest;
import com.inhubber.identity.dto.LoginResponse;
import com.inhubber.identity.dto.RegisterRequest;
import com.inhubber.identity.dto.SyncEmailRequest;
import com.inhubber.identity.dto.ValidateResponse;
import com.inhubber.identity.service.AuthService;
import com.inhubber.identity.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

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

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Claims claims = jwtService.parseToken(token);
        return ResponseEntity.ok(new ValidateResponse(claims.getSubject(), claims.get("role", String.class)));
    }

    @PatchMapping("/users/email")
    public ResponseEntity<Void> syncEmail(@RequestBody @Valid SyncEmailRequest request) {
        authService.updateEmail(request.oldEmail(), request.newEmail());
        return ResponseEntity.ok().build();
    }
}
