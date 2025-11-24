package com.everyones_coupon.dto;

import com.everyones_coupon.domain.Coupon;
import com.everyones_coupon.domain.CouponStatusEnum;
import com.everyones_coupon.domain.Game;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "쿠폰 번호는 필수입니다.")
    private String code;        // UI: 쿠폰 번호

    @NotBlank(message = "쿠폰 보상은 필수입니다.")
    private String reward;      // UI: 쿠폰 보상

    @NotNull(message = "만료 날짜는 필수입니다.")
    @Future(message = "만료 날짜는 현재보다 미래여야 합니다.") // 과거 날짜 입력 방지
    // 프론트엔드에서 "yyyy.MM.dd" 형식(예: 2025.12.31)으로 보내야 함
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private LocalDate expireDate; // UI: 만료 날짜

    // DTO -> Entity 변환 메서드
    public Coupon toEntity(Game game) {
        return Coupon.builder()
                .game(game)                   // 연관된 게임 객체 매핑
                .code(this.code)
                .reward(this.reward)
                // LocalDate를 LocalDateTime(00:00:00)으로 변환
                .expirationDate(this.expireDate.atTime(java.time.LocalTime.MAX)) 
                .status(CouponStatusEnum.VALID) // 기본 상태: 유효
                .score(0.0)
                .validCount(0)
                .invalidCount(0)
                .build();
    }
}