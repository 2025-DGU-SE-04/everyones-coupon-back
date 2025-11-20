package com.everyones_coupon.repository;

import com.everyones_coupon.domain.AdminToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminTokenRepository extends JpaRepository<AdminToken, String> {
    // 토큰 존재 여부 확인 (인증용)
    boolean existsByToken(String token);
}
