package com.everyones_coupon.dto;

import com.everyones_coupon.domain.Game;
import lombok.Getter;

@Getter
public class GameResponse {
    private Long id;
    private String title;
    private String gameImageUrl;    // 게임 이미지
    private int viewCount;          // 조회수
    private int couponCount;        // 현재 등록된 쿠폰 개수

    // Entity -> DTO 변환 생성자
    public GameResponse(Game game) {
        this.id = game.getId();
        this.title = game.getTitle();
        this.gameImageUrl = game.getGameImageUrl();
        this.viewCount = game.getViewCount();
        this.couponCount = game.getCouponCount();
    }
}