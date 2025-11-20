package com.everyones_coupon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Game extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String gameDescription;
    private String couponUsageLink;
    private String category;

    private boolean official;
    private String gameImageUrl;

    private int couponCount;
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