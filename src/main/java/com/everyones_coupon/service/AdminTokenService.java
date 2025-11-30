package com.everyones_coupon.service;

import com.everyones_coupon.domain.AdminToken;
import com.everyones_coupon.repository.AdminTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AdminTokenService {

    private final AdminTokenRepository adminTokenRepository;

    public String createAdminToken(String token, String description) {
        String t = token;
        if (t == null || t.isBlank()) {
            // Secure random token instead of UUID; we'll use Base64 of random bytes
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            t = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
        if (adminTokenRepository.existsByToken(t)) {
            return t; // already exists, return existing
        }
        AdminToken adminToken = AdminToken.builder()
                .token(t)
                .description(description)
                .build();
        adminTokenRepository.save(adminToken);
        return t;
    }
}
