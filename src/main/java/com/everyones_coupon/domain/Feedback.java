package com.everyones_coupon.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "feedbacks", uniqueConstraints = {
    // 한 쿠폰(coupon_id)에 대해 같은 IP(ip_address)는 단 하나의 피드백만 남길 수 있음
    @UniqueConstraint(
        name = "uk_feedback_coupon_ip", 
        columnNames = {"coupon_id", "ipAddress"}
    )
})

public class Feedback extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Coupon coupon;

    @Column(nullable = true)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackStatusEnum status;

    public void updateStatus(FeedbackStatusEnum status) {
        this.status = status;
    }
}
