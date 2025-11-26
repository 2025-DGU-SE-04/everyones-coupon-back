package com.everyones_coupon.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupon_game_id", columnList = "game_id"),
    @Index(name = "idx_coupon_score", columnList = "score"),
    @Index(name = "idx_coupon_created_at", columnList = "createdAt")
})
public class Coupon extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Game game;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String reward;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String detail;

    @Column(nullable = true)
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatusEnum status;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private int validCount;

    @Column(nullable = false)
    private int invalidCount;

    // --- 비즈니스 로직 ---

    public void updateStatus(CouponStatusEnum status) {
        this.status = status;
    }

    public void increaseValidCount() {
        this.validCount++;
        updateScore();
    }

    public void increaseInvalidCount() {
        this.invalidCount++;
        updateScore();
    }

    public void decreaseValidCount() {
        if (this.validCount > 0) {
            this.validCount--;
            updateScore(); // 점수 재계산
        }
    }

    public void decreaseInvalidCount() {
        if (this.invalidCount > 0) {
            this.invalidCount--;
            updateScore(); // 점수 재계산
        }
    }

    private void updateScore() {
        // 간단한 신뢰도 점수 계산 로직 (예시)
        // valid의 비율을 백분율로 환산
        // TODO: 지수가중이동평균으로 개선 필요
        int total = validCount + invalidCount;
        if (total > 0) {
            this.score = (double) validCount / total * 100;
        } else {
            this.score = 0;
        }
    }
}
