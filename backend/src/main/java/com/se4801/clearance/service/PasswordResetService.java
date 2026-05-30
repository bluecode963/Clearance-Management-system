package com.se4801.clearance.service;

import com.se4801.clearance.dto.response.PasswordResetResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.model.PasswordResetToken;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.PasswordResetTokenRepository;
import com.se4801.clearance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String GENERIC_MESSAGE =
            "If the email exists, a password reset process has been started.";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Transactional
    public PasswordResetResponse startReset(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(this::createResetToken)
                .orElseGet(() -> new PasswordResetResponse(GENERIC_MESSAGE, null));
    }

    @Transactional
    public PasswordResetResponse resetPassword(String token, String newPassword) {
        String tokenHash = hashToken(token);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Password reset token is invalid or expired"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Password reset token is invalid or expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.setUsed(true);
        resetToken.setUsedAt(Instant.now());

        return new PasswordResetResponse("Password has been reset successfully.", null);
    }

    private PasswordResetResponse createResetToken(User user) {
        String rawToken = generateRawToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plusSeconds(30 * 60))
                .build();
        passwordResetTokenRepository.save(resetToken);

        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            return new PasswordResetResponse(GENERIC_MESSAGE, rawToken);
        }

        System.out.println("Password reset token for " + user.getEmail() + ": " + rawToken);
        return new PasswordResetResponse(GENERIC_MESSAGE, null);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
