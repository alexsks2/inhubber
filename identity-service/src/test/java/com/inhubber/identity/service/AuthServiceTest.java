package com.inhubber.identity.service;

import com.inhubber.identity.domain.Role;
import com.inhubber.identity.domain.User;
import com.inhubber.identity.exception.EmailAlreadyRegisteredException;
import com.inhubber.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returnsToken() {
        User user = new User("user@acme.com", "hashed", Role.USER);
        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("user@acme.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.login("user@acme.com", "password123");

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void login_wrongPassword_throwsIllegalArgumentException() {
        User user = new User("user@acme.com", "hashed", Role.USER);
        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("user@acme.com", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid credentials");
    }

    @Test
    void login_userNotFound_throwsIllegalArgumentException() {
        when(userRepository.findByEmail("unknown@acme.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown@acme.com", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid credentials");
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyRegisteredException() {
        when(userRepository.existsByEmail("user@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("user@acme.com", "password123", Role.USER))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
