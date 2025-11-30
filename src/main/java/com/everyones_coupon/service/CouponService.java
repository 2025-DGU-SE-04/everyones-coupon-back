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
import java.util.Optional;

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
    public Page<CouponResponse> getCouponsByGame(Long gameId, Pageable pageable, String ipAddress) {
        Page<Coupon> coupons = couponRepository.findByGameId(gameId, pageable);
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
        return couponRepository.findTop10ByGameIdOrderByScoreDesc(gameId).stream()
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
    public void voteCoupon(Long couponId, boolean isWorking, String ipAddress) {
        // 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        // 사용자의 기존 투표 내역 조회
        Optional<Feedback> existingFeedback = feedbackRepository.findByCouponIdAndIpAddress(couponId, ipAddress);

        FeedbackStatusEnum newStatus = isWorking ? FeedbackStatusEnum.VALID : FeedbackStatusEnum.INVALID;

        if (existingFeedback.isPresent()) {
            // 이미 투표한 이력이 있는 경우 -> 수정 로직
            Feedback feedback = existingFeedback.get();

            // 기존 상태와 다를 경우에만 처리
            if (feedback.getStatus() != newStatus) {
                // 기존 투표 취소
                if (feedback.getStatus() == FeedbackStatusEnum.VALID) {
                    coupon.decreaseValidCount();
                } else {
                    coupon.decreaseInvalidCount();
                }

                // 새로운 투표 반영
                if (newStatus == FeedbackStatusEnum.VALID) {
                    coupon.increaseValidCount();
                } else {
                    coupon.increaseInvalidCount();
                }

                // 피드백 상태 업데이트
                feedback.updateStatus(newStatus);
            }

        } else {
            // 첫 투표인 경우 -> 생성 로직
            if (newStatus == FeedbackStatusEnum.VALID) {
                coupon.increaseValidCount();
            } else {
                coupon.increaseInvalidCount();
            }

            Feedback feedback = Feedback.builder()
                    .coupon(coupon)
                    .ipAddress(ipAddress)
                    .status(newStatus)
                    .build();

            feedbackRepository.save(feedback);
        }
    }
}