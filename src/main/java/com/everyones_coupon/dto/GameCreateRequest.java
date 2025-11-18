package com.everyones_coupon.dto;

import com.everyones_coupon.domain.Game;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameCreateRequest {

    @NotBlank(message = "게임 이름은 필수입니다.")
    private String title;            // 게임 이름

    @NotBlank(message = "쿠폰 사용 방법은 필수입니다.")
    private String gameDescription;  // 쿠폰 사용 방법

    private String couponUsageLink;  // 쿠폰 사용 링크

    private String category;

    // DTO -> Entity 변환 메서드 
    public Game toEntity() {
        return Game.builder()
                .title(this.title)
                .gameDescription(this.gameDescription)
                .couponUsageLink(this.couponUsageLink)
                .category(this.category)
                .viewCount(0)
                .couponCount(0)
                .official(false)
                .build();
    }
}