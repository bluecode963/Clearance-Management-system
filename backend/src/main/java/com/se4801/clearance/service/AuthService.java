package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.LoginRequest;
import com.se4801.clearance.dto.request.RegisterRequest;
import com.se4801.clearance.dto.response.AuthResponse;
import com.se4801.clearance.dto.response.UserResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.mapper.UserMapper;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.StudentProfile;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.StudentProfileRepository;
import com.se4801.clearance.repository.UserRepository;
import com.se4801.clearance.security.CustomUserPrincipal;
import com.se4801.clearance.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Email is already registered");
        }

        validateStudentProfileFields(request);

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        if (request.role() == Role.STUDENT) {
            createStudentProfile(request, savedUser);
        }

        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive");
        }

        if (user.getRole() != request.role()) {
            throw new BadCredentialsException("Selected role does not match this account");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String token) {
        tokenBlacklistService.blacklist(normalizeToken(token));
    }

    public UserResponse getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new CustomUserPrincipal(user));
        return new AuthResponse(token, TOKEN_TYPE, UserMapper.toResponse(user));
    }

    private void validateStudentProfileFields(RegisterRequest request) {
        if (request.role() != Role.STUDENT) {
            return;
        }

        if (isBlank(request.studentId())) {
            throw new BusinessRuleException("Student ID is required for student registration");
        }
        if (isBlank(request.department())) {
            throw new BusinessRuleException("Department is required for student registration");
        }
        if (isBlank(request.program())) {
            throw new BusinessRuleException("Program is required for student registration");
        }
        if (request.yearOfStudy() == null) {
            throw new BusinessRuleException("Year of study is required for student registration");
        }
        if (studentProfileRepository.findByStudentId(request.studentId().trim()).isPresent()) {
            throw new BusinessRuleException("Student ID is already registered");
        }
    }

    private void createStudentProfile(RegisterRequest request, User user) {
        StudentProfile profile = StudentProfile.builder()
                .studentId(request.studentId().trim())
                .department(request.department().trim())
                .program(request.program().trim())
                .yearOfStudy(request.yearOfStudy())
                .user(user)
                .build();
        studentProfileRepository.save(profile);
    }

    private String normalizeToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
