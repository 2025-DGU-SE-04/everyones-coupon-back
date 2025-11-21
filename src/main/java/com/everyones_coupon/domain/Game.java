package com.everyones_coupon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "games", indexes = {
    @Index(name = "idx_game_title", columnList = "title"),
    @Index(name = "idx_game_view_count", columnList = "viewCount")
})
public class Game extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String gameDescription;

    @Column(nullable = true)
    private String couponUsageLink;

    @Column(nullable = true)
    private String category;

    @Column(nullable = false)
    private boolean official;

    @Column(nullable = true)
    private String gameImageUrl;

    @Column(nullable = false)
    private int couponCount;

    @Column(nullable = false)
    private int viewCount;

    public void markOfficial() {
        this.official = true;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
    
    public void increaseCouponCount() {
        this.couponCount++;
    }
}