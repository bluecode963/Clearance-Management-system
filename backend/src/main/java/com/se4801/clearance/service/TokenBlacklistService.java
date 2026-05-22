package com.se4801.clearance.service;

import com.se4801.clearance.model.BlacklistedToken;
import com.se4801.clearance.repository.BlacklistedTokenRepository;
import com.se4801.clearance.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public void blacklist(String token) {
        if (isBlacklisted(token)) {
            return;
        }

        Instant expiryDate = jwtService.extractExpiration(token).toInstant();
        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .expiryDate(expiryDate)
                .build();
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }
}
