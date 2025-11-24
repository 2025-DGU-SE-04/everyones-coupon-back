package com.everyones_coupon.repository;

import com.everyones_coupon.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // 중복 투표 확인용 메서드 (exists 쿼리 사용으로 성능 최적화)
    boolean existsByCouponIdAndIpAddress(Long couponId, String ipAddress);
}