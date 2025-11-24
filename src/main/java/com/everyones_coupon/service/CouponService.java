package com.everyones_coupon.service;

import com.everyones_coupon.domain.Coupon;
import com.everyones_coupon.domain.Feedback;
import com.everyones_coupon.domain.FeedbackStatusEnum;
import com.everyones_coupon.domain.Game;
import com.everyones_coupon.dto.CouponCreateRequest;
import com.everyones_coupon.dto.CouponResponse;
import com.everyones_coupon.repository.CouponRepository;
import com.everyones_coupon.repository.FeedbackRepository;
import com.everyones_coupon.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final GameRepository gameRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Long addCoupon(Long gameId, CouponCreateRequest request) {
        // 게임 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게임을 찾을 수 없습니다. id=" + gameId));

        // 중복 코드 검사
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("이미 등록된 쿠폰 코드입니다.");
        }

        // Entity 변환 및 저장
        Coupon coupon = request.toEntity(game);
        Coupon savedCoupon = couponRepository.save(coupon);

        // 게임의 쿠폰 개수 증가
        game.increaseCouponCount();

        return savedCoupon.getId();
    }

    /*
     특정 게임의 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<CouponResponse> getCouponsByGame(Long gameId, Pageable pageable) {
        Page<Coupon> coupons = couponRepository.findByGameId(gameId, pageable);
        // Entity -> DTO 변환
        return coupons.map(CouponResponse::new);
    }

    /*
     Score가 높은 상위 10개 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getTopCoupons(Long gameId) {
        return couponRepository.findTop10ByGameIdOrderByScoreDesc(gameId).stream()
                .map(CouponResponse::new)
                .collect(Collectors.toList());
    }
    
    /*
     유효성 투표
     */
    @Transactional
    public void voteCoupon(Long couponId, boolean isWorking, String ipAddress) {
        // 중복 투표 검사
        if (feedbackRepository.existsByCouponIdAndIpAddress(couponId, ipAddress)) {
            throw new IllegalStateException("이미 참여한 투표입니다.");
        }

        // 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        // 쿠폰 상태 변경
        if (isWorking) {
            coupon.increaseValidCount();
        } else {
            coupon.increaseInvalidCount();
        }

        // 피드백 로그 저장
        Feedback feedback = Feedback.builder()
                .coupon(coupon)
                .ipAddress(ipAddress)
                .status(isWorking ? FeedbackStatusEnum.VALID : FeedbackStatusEnum.INVALID)
                .build();
        
        feedbackRepository.save(feedback);
    }
}