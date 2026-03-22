package com.inhubber.identity.service;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.domain.User;
import com.inhubber.identity.exception.EmailAlreadyRegisteredException;
import com.inhubber.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        return jwtService.generateToken(user.getEmail(), user.getRole());
    }

    public void register(String email, String password, Role role) {
        if (existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        User user = new User(email, passwordEncoder.encode(password), role);
        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
