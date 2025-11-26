# everyones-coupon

## 개발 환경

- **JDK**: 17
- **Spring Boot**: 3.5.7
- **Gradle**: 8.14.3 (Groovy DSL)

## 데이터베이스 ERD
> **참고**: Mermaid 문법이 `NOT NULL`, `INDEX` 등의 제약조건을 완벽하게 지원하지 않아, 컬럼 설명란에 다음과 같이 명시했습니다.
> - `(Not Null)` / `(Nullable)`: Null 허용 여부
> - `[IDX]`: 일반 인덱스 (Index)
> - `[UK]`: 유니크 인덱스 (Unique Key)

```mermaid
erDiagram
    %% 1. 관리자 토큰 테이블 (단일 인증 수단)
    ADMIN_TOKENS {
        varchar token PK "관리자 인증 토큰 (비밀번호)"
        varchar description "토큰 설명 (예: 마스터키) (Nullable)"
        timestamp created_at "(Not Null)"
        timestamp updated_at "(Not Null)"
    }

    %% 2. 게임 테이블 (Class: Game)
    GAMES {
        bigint id PK
        varchar title "게임 이름 (Not Null) [IDX]"
        text description "게임 설명 (Nullable)"
        varchar coupon_usage_link "쿠폰 사용처 링크 (Nullable)"
        boolean is_official "공식 게임 여부 (Not Null)"
        varchar game_image_url "이미지/배너 URL (Nullable)"
        int coupon_count "쿠폰 수 (Not Null)"
        int view_count "조회수 (Not Null) [IDX]"
        timestamp created_at "(Not Null)"
        timestamp updated_at "(Not Null)"
    }

    %% 3. 쿠폰 테이블 (Class: Coupon)
    COUPONS {
        bigint id PK
        bigint game_id FK "소속 게임 ID (Not Null) [IDX]"
        varchar code "쿠폰 코드 (Not Null) [UK]"
        varchar reward "보상 내용 (Not Null)"
        text detail "상세 설명 (Nullable)"
        datetime expiration_date "만료일 (Nullable)"
        enum status "VALID/INVALID/EXPIRED (Not Null)"
        double score "신뢰도 점수 (Not Null) [IDX]"
        int valid_count "유효함 수 (Not Null)"
        int invalid_count "유효하지 않음 수 (Not Null)"
        timestamp created_at "(Not Null) [IDX]"
        timestamp updated_at "(Not Null)"
    }

    %% 4. 피드백 테이블 (Class: Feedback)
    FEEDBACKS {
        bigint id PK
        bigint coupon_id FK "대상 쿠폰 ID (Not Null)"
        varchar ip_address "작성자 식별(IP) (Nullable)"
        enum status "VALID/INVALID (Not Null)"
        timestamp created_at "(Not Null)"
        timestamp updated_at "(Not Null)"
    }

    %% 5. 차단 클라이언트 테이블 (Class: AbuseChecker)
    BANNED_CLIENTS {
        bigint id PK
        varchar ip_address "차단된 IP (Not Null)"
        datetime banned_until "차단 해제일 (Not Null)"
        varchar reason "차단 사유 (Nullable)"
        timestamp created_at "(Not Null)"
    }

    %% 6. 관리자 세션 테이블 (Class: AdminSession) - 세션 기반 쿠키 저장
    ADMIN_SESSIONS {
        bigint id PK
        varchar session_id "랜덤 세션 UUID (Not Null) [UK]"
        varchar token FK "관리자 토큰 (AdminToken의 token) (Not Null)"
        timestamp created_at "(Not Null)"
        timestamp expires_at "(Not Null)"
    }

    %% 관계 정의
    GAMES ||--o{ COUPONS : "contains (1:N)"
    COUPONS ||--o{ FEEDBACKS : "receives (1:N)"
    ADMIN_TOKENS ||--o{ ADMIN_SESSIONS : "issues (1:N)"
```
