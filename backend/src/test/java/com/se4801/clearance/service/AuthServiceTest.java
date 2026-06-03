package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.LoginRequest;
import com.se4801.clearance.dto.response.AuthResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.StudentProfileRepository;
import com.se4801.clearance.repository.UserRepository;
import com.se4801.clearance.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginSucceedsWhenEmailPasswordAndRoleMatch() {
        User user = user(Role.STUDENT, true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest(
                "Student@Test.com",
                "password123",
                Role.STUDENT
        ));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().role()).isEqualTo(Role.STUDENT);
        verify(passwordEncoder).matches("password123", user.getPasswordHash());
    }

    @Test
    void loginFailsWhenSelectedRoleDoesNotMatchAccountRole() {
        User user = user(Role.STUDENT, true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest(
                "student@test.com",
                "password123",
                Role.ADMIN
        )))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Selected role does not match this account");
    }

    @Test
    void loginFailsWhenUserIsInactive() {
        User user = user(Role.STUDENT, false);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest(
                "student@test.com",
                "password123",
                Role.STUDENT
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User account is inactive");
    }

    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        User user = user(Role.STUDENT, true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(
                "student@test.com",
                "wrong-password",
                Role.STUDENT
        )))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder).matches("wrong-password", user.getPasswordHash());
    }

    private User user(Role role, boolean active) {
        return User.builder()
                .id(1L)
                .fullName("Test User")
                .email("student@test.com")
                .passwordHash("$2a$10$hashed")
                .role(role)
                .active(active)
                .build();
    }
}
