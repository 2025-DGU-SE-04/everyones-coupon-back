package com.everyones_coupon.service;

import com.everyones_coupon.domain.Coupon;
import com.everyones_coupon.domain.Feedback;
import com.everyones_coupon.domain.FeedbackStatusEnum;
import com.everyones_coupon.domain.Game;
import com.everyones_coupon.dto.CouponCreateRequest;
import com.everyones_coupon.dto.CouponResponse;
import com.everyones_coupon.dto.VoteResponse;
import com.everyones_coupon.repository.CouponRepository;
import com.everyones_coupon.repository.FeedbackRepository;
import com.everyones_coupon.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final GameRepository gameRepository;
    private final FeedbackRepository feedbackRepository;

    // 유효성 커트라인 (이 점수보다 낮으면 목록에서 숨김)
    private static final double MIN_SCORE_THRESHOLD = -10.0;

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
    public Page<CouponResponse> getCouponsByGame(Long gameId, Pageable pageable, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        Page<Coupon> coupons = couponRepository.findActiveCoupons(gameId, now, MIN_SCORE_THRESHOLD, pageable);
        // Entity -> DTO 변환
        return coupons.map(coupon -> {
            FeedbackStatusEnum myVote = getMyVoteStatus(coupon.getId(), ipAddress);
            return new CouponResponse(coupon, myVote);
        });
    }

    private FeedbackStatusEnum getMyVoteStatus(Long couponId, String ipAddress) {
        return feedbackRepository.findByCouponIdAndIpAddress(couponId, ipAddress)
                .map(feedback -> feedback.getStatus()) // 투표 했으면 상태(VALID/INVALID) 반환
                .orElse(null); // 투표 안 했으면 null 반환
    }

    /*
     Score가 높은 상위 10개 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getTopCoupons(Long gameId, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        
        Pageable limitTen = PageRequest.of(0, 10);
        List<Coupon> topCoupons = couponRepository.findTop10ActiveCoupons(gameId, now, limitTen);

        return topCoupons.stream()
                .map(coupon -> {
                    FeedbackStatusEnum myVote = getMyVoteStatus(coupon.getId(), ipAddress);
                    return new CouponResponse(coupon, myVote);
                })
                .collect(Collectors.toList());
    }
    
    /*
     유효성 투표
     */
    @Transactional
    public VoteResponse voteCoupon(Long couponId, boolean isWorking, String ipAddress) {
        // 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        // 기존 투표 내역 조회
        Optional<Feedback> existingFeedback = feedbackRepository.findByCouponIdAndIpAddress(couponId, ipAddress);
        FeedbackStatusEnum targetStatus = isWorking ? FeedbackStatusEnum.VALID : FeedbackStatusEnum.INVALID;
        FeedbackStatusEnum finalStatus;

        if (existingFeedback.isPresent()) {
            Feedback feedback = existingFeedback.get();

            if (feedback.getStatus() == targetStatus) {
                if (targetStatus == FeedbackStatusEnum.VALID) coupon.decreaseValidCount();
                else coupon.decreaseInvalidCount();
                
                feedbackRepository.delete(feedback);
                finalStatus = null;

            } else {
                if (feedback.getStatus() == FeedbackStatusEnum.VALID) coupon.decreaseValidCount();
                else coupon.decreaseInvalidCount();

                if (targetStatus == FeedbackStatusEnum.VALID) coupon.increaseValidCount();
                else coupon.increaseInvalidCount();

                feedback.updateStatus(targetStatus);
                finalStatus = targetStatus;
            }
        } else {
            if (targetStatus == FeedbackStatusEnum.VALID) coupon.increaseValidCount();
            else coupon.increaseInvalidCount();

            Feedback feedback = Feedback.builder()
                    .coupon(coupon)
                    .ipAddress(ipAddress)
                    .status(targetStatus)
                    .build();
            feedbackRepository.save(feedback);
            finalStatus = targetStatus;
        }

        return new VoteResponse(
                finalStatus,
                coupon.getValidCount(),
                coupon.getInvalidCount()
        );
    }
}