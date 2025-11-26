package com.everyones_coupon.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CouponTest {

    @Test
    @DisplayName("쿠폰 생성 시 초기 점수는 0점이다")
    void createCoupon() {
        Coupon coupon = Coupon.builder()
                .code("TEST-CODE")
                .reward("1000 Gold")
                .status(CouponStatusEnum.VALID)
                .build();

        assertThat(coupon.getScore()).isEqualTo(0.0);
        assertThat(coupon.getValidCount()).isEqualTo(0);
        assertThat(coupon.getInvalidCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요를 누르면 좋아요 수가 증가하고 점수가 갱신된다")
    void increaseValidCount() {
        // given
        Coupon coupon = Coupon.builder()
                .code("TEST-CODE")
                .status(CouponStatusEnum.VALID)
                .build();

        // when
        coupon.increaseValidCount(); // 좋아요 1, 싫어요 0 -> 점수 100점

        // then
        assertThat(coupon.getValidCount()).isEqualTo(1);
        assertThat(coupon.getScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("싫어요를 누르면 싫어요 수가 증가하고 점수가 갱신된다")
    void increaseInvalidCount() {
        // given
        Coupon coupon = Coupon.builder()
                .code("TEST-CODE")
                .status(CouponStatusEnum.VALID)
                .build();

        // when
        coupon.increaseValidCount();    // 좋아요 1 (100점)
        coupon.increaseInvalidCount(); // 싫어요 1 (총 2개 중 좋아요 1개 -> 50점)

        // then
        assertThat(coupon.getInvalidCount()).isEqualTo(1);
        assertThat(coupon.getScore()).isEqualTo(50.0);
    }
}
