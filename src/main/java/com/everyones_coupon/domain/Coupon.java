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

    private static final double ALPHA = 0.3;

    public void updateStatus(CouponStatusEnum status) {
        this.status = status;
    }

    public void increaseValidCount() {
        this.validCount++;
        updateScore(100.0);
    }

    public void increaseInvalidCount() {
        this.invalidCount++;
        updateScore(0.0);
    }

    public void decreaseValidCount() {
        if (this.validCount > 0) {
            this.validCount--;
        }
    }

    public void decreaseInvalidCount() {
        if (this.invalidCount > 0) {
            this.invalidCount--;
        }
    }

    private void updateScore(double newSample) {
        int total = validCount + invalidCount;

        if (total == 1) {
            // 첫 투표라면 평균을 낼 과거 데이터가 없으므로 입력값 그대로 설정
            this.score = newSample;
        } else {
            this.score = (ALPHA * newSample) + ((1.0 - ALPHA) * this.score);
        }
    }
}
