package com.inhubber.identity.service;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.domain.User;
import com.inhubber.identity.exception.EmailAlreadyRegisteredException;
import com.inhubber.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public Boolean login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        return true;
    }

    public void register(String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User user = new User(email, password, role);
        userRepository.save(user);
    }
}
