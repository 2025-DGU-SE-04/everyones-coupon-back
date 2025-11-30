package com.everyones_coupon.dto;

import com.everyones_coupon.domain.Coupon;
import com.everyones_coupon.domain.CouponStatusEnum;
import com.everyones_coupon.domain.Feedback;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.everyones_coupon.domain.FeedbackStatusEnum;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class CouponResponse {

    private Long id;
    private String code;            // 쿠폰 코드 (복사 대상)
    private String reward;          // 보상 내용 (예: 다이아 100개)
    private String detail;          // 상세 설명

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private LocalDateTime expirationDate; // 만료 날짜 (YYYY.MM.DD 형식)

    private String dDay;            // [계산 필드] D-Day (예: "D-5", "만료됨")

    private CouponStatusEnum status;// 상태 (VALID, INVALID, EXPIRED)
    private FeedbackStatusEnum myVote;
    
    private double score;           // 신뢰도 점수
    private int validCount;          // 유효함 수
    private int invalidCount;       // 유효하지않음 수

    private LocalDateTime createdAt; // 등록일 (최신순 정렬용)

    // Entity -> DTO 변환 생성자 (여기서 UI 로직 처리)
    public CouponResponse(Coupon coupon, FeedbackStatusEnum myVote) {
        this.id = coupon.getId();
        this.code = coupon.getCode();
        this.reward = coupon.getReward();
        this.detail = coupon.getDetail();
        this.expirationDate = coupon.getExpirationDate();
        this.status = coupon.getStatus();
        this.score = coupon.getScore();
        this.validCount = coupon.getValidCount();
        this.invalidCount = coupon.getInvalidCount();
        this.createdAt = coupon.getCreatedAt();

        // D-Day 계산 로직
        this.dDay = calculateDDay(coupon.getExpirationDate());

        this.myVote = myVote;
    }

    private String calculateDDay(LocalDateTime expirationDate) {
        if (expirationDate == null) {
            return "무제한";
        }

        LocalDate today = LocalDate.now();
        LocalDate expireDay = expirationDate.toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(today, expireDay);

        if (daysBetween < 0) {
            return "만료됨";
        } else if (daysBetween == 0) {
            return "0일";
        } else {
            return daysBetween + "일";
        }
    }
}