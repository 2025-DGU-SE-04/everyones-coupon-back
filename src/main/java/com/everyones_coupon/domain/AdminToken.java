package com.everyones_coupon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "admin_tokens")
public class AdminToken extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String token; // 관리자 인증 토큰 (비밀번호 역할, PK)

    @Column(nullable = true)
    private String description; // 토큰 설명 (예: 마스터키, 1팀 관리자 등)
}
