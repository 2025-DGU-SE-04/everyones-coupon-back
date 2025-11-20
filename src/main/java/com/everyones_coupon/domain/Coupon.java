package com.everyones_coupon.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String reward;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatusEnum status;

    private double score;
    private int likeCount;
    private int dislikeCount;

    @OneToMany(mappedBy = "coupon")
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();

    // --- 비즈니스 로직 ---

    public void updateStatus(CouponStatusEnum status) {
        this.status = status;
    }

    public void increaseLikeCount() {
        this.likeCount++;
        updateScore();
    }

    public void increaseDislikeCount() {
        this.dislikeCount++;
        updateScore();
    }

    private void updateScore() {
        // 간단한 신뢰도 점수 계산 로직 (예시)
        // like의 비율을 백분율로 환산
        // TODO: 지수가중이동평균으로 개선 필요
        int total = likeCount + dislikeCount;
        if (total > 0) {
            this.score = (double) likeCount / total * 100;
        } else {
            this.score = 0;
        }
    }
}
