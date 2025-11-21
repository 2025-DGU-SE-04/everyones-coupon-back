package com.everyones_coupon.repository;

import com.everyones_coupon.domain.Coupon;
import com.everyones_coupon.domain.CouponStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 특정 게임의 쿠폰 목록 조회 (페이징)
    Page<Coupon> findByGameId(Long gameId, Pageable pageable);

    // 특정 게임의 특정 상태 쿠폰 목록 조회 (예: 유효한 쿠폰만)
    Page<Coupon> findByGameIdAndStatus(Long gameId, CouponStatusEnum status, Pageable pageable);

    // 특정 게임의 쿠폰을 최신순으로 상위 N개 조회 (리스트 반환)
    List<Coupon> findTop10ByGameIdOrderByCreatedAtDesc(Long gameId);

    // 특정 게임의 쿠폰을 점수순(신뢰도순)으로 상위 N개 조회
    List<Coupon> findTop10ByGameIdOrderByScoreDesc(Long gameId);

    // 쿠폰 코드로 조회 (중복 검사 등)
    boolean existsByCode(String code);
}
