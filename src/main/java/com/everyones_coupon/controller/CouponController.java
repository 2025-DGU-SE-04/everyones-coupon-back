package com.everyones_coupon.controller;

import com.everyones_coupon.dto.CouponCreateRequest;
import com.everyones_coupon.dto.CouponResponse;
import com.everyones_coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 쿠폰 생성 API
     * POST /api/coupons?gameId={}
     */
    @PostMapping
    public ResponseEntity<Long> createCoupon(
            @RequestParam("gameId") Long gameId,
            @RequestBody @Valid CouponCreateRequest request) {
        
        Long couponId = couponService.addCoupon(gameId, request);
        return ResponseEntity.ok(couponId);
    }

    /**
     * 특정 게임의 쿠폰 목록 조회
     * GET /api/coupons?gameId=1&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getCoupons(
            @RequestParam("gameId") Long gameId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, HttpServletRequest request) {
                
        String clientIp = getClientIp(request);
        Page<CouponResponse> coupons = couponService.getCouponsByGame(gameId, pageable);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 특정 게임의 신뢰도 상위 쿠폰 10개 조회
     * GET /api/coupons/top?gameId=1
     */
    @GetMapping("/top")
    public ResponseEntity<List<CouponResponse>> getTopCoupons(
            @RequestParam("gameId") Long gameId, HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        List<CouponResponse> topCoupons = couponService.getTopCoupons(gameId);
        return ResponseEntity.ok(topCoupons);
    }

    /**
     * 유효성 투표 API
     * POST /api/coupons/{couponId}/vote?isWorking=true
     */
    @PostMapping("/{couponId}/vote")
    public ResponseEntity<Void> voteCoupon(
            @PathVariable("couponId") Long couponId,
            @RequestParam("isWorking") boolean isWorking,
            HttpServletRequest request) { // IP 추출을 위해 request 객체 필요
        
        String ipAddress = getClientIp(request);
        couponService.voteCoupon(couponId, isWorking, ipAddress);
        
        return ResponseEntity.ok().build();
    }

    // --- Utility Method: IP 추출 ---
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Azure나 AWS 같은 로드밸런서를 타면 IP가 콤마(,)로 여러 개 찍힐 수 있음.
        // 첫 번째 IP가 실제 클라이언트 IP임.
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        
        return ip;
    }
}