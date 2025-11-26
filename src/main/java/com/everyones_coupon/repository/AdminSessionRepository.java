package com.everyones_coupon.repository;

import com.everyones_coupon.domain.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminSessionRepository extends JpaRepository<AdminSession, Long> {
    Optional<AdminSession> findBySessionId(String sessionId);
}
