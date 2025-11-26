# 관리자 API 명세

다음은 관리자용 API 5개에 대한 간단한 명세입니다. (담당자 정보는 포함하지 않습니다.)

---

## 1. 게임 삭제
- 메서드: DELETE
- 엔드포인트: `/api/admin/games/{gameId}`
- 상태: 시작 전
- 파라미터 (Path):
  - `gameId` (Long): 삭제할 게임의 ID
- 응답 코드 예시:
  - 200 OK: 정상 삭제
  - 404 Not Found: 해당 게임이 없는 경우
  - 401 Unauthorized: 관리자 인증 실패

---

## 2. 쿠폰 삭제
- 메서드: DELETE
- 엔드포인트: `/api/admin/coupons/{couponId}`
- 상태: 시작 전
- 파라미터 (Path):
  - `couponId` (Long): 삭제할 쿠폰의 ID
- 응답 코드 예시:
  - 200 OK: 정상 삭제
  - 404 Not Found: 해당 쿠폰이 없는 경우
  - 401 Unauthorized: 관리자 인증 실패

---

## 3. Official 마크 등록
- 메서드: POST
- 엔드포인트: `/api/admin/games/{gameId}/official`
- 상태: 시작 전
- 파라미터 (Path):
  - `gameId` (Long): 대상 게임 ID
- 파라미터 (Body):
  - `official` (boolean, Required): Official 마크 설정 값 (true/false)
- 응답 코드 예시:
  - 200 OK: 정상 반영
  - 404 Not Found: 해당 게임이 없는 경우
  - 400 Bad Request: 잘못된 입력
  - 401 Unauthorized: 관리자 인증 실패

---

## 4. 관리자 로그인
- 메서드: POST
- 엔드포인트: `/api/admin/login`
- 상태: 시작 전
- 파라미터 (Body):
  - `token` (String, Required): 관리자 인증 토큰
- 응답 코드 예시:
  - 200 OK: 로그인 성공
  - 401 Unauthorized: 토큰 불일치 또는 인증 실패
 - 동작 상세:
   - 로그인에 성공하면 서버는 `ADMIN_SESSION` 쿠키(HTTP-Only)를 발급합니다. 이 쿠키는 토큰 자체를 담지 않으며, 서버에서 생성한 세션 식별자(session id)를 저장합니다.
   - 관리자 토큰은 `AdminToken` DB 컬럼에 리터럴로 저장되어 인증에 사용됩니다.

---

## 5. 관리자 이미지 업로드
- 메서드: POST
- 엔드포인트: `/api/admin/games/{gameId}/image`
- 상태: 시작 전
- 파라미터 (Path):
  - `gameId` (Long): 게임 ID
- 파라미터 (Body):
  - `imageData` (String, Required): Base64로 인코딩된 이미지 데이터
- 응답 코드 예시:
  - 200 OK: 업로드 성공 (업로드된 이미지 URL 또는 ID 반환 권장)
  - 400 Bad Request: 잘못된 이미지 데이터
  - 401 Unauthorized: 관리자 인증 실패

---

## 6. 관리자 로그아웃
- 메서드: POST
- 엔드포인트: `/api/admin/logout`
- 상태: 시작 전
- 파라미터 (Header):
  - Authorization: `Bearer <token>` (선택) — 관리자 토큰이 제공되면 해당 토큰 기준으로 연동된 모든 세션을 무효화합니다.
- 파라미터 (Cookie):
  - `ADMIN_SESSION` (String, 선택): 로그인 시 발급된 세션 식별자. 제공될 경우 해당 세션만 무효화합니다.
- 동작 상세:
  - 토큰이 제공되면 해당 토큰과 연동된 모든 세션을 무효화(로그아웃)합니다.
  - cookie가 제공되면 해당 세션만 무효화합니다.
  - 응답 시 `ADMIN_SESSION` 쿠키를 만료(Max-Age=0)시켜 브라우저에서 제거하도록 합니다.
- 응답 코드 예시:
  - 200 OK: 로그아웃 성공 (쿠키 만료 및 세션 무효화)
  - 401 Unauthorized: 토큰/세션이 유효하지 않은 경우

---

---

## 공통 고려사항
- 인증: 모든 관리자 엔드포인트는 관리자 인증 토큰 또는 세션을 요구합니다.
- 권한 검증: 관리자 계정에 따른 권한 검증(예: 특정 게임 삭제 권한) 필요 시 추가 검증 필요.
- 입력 검증: Path/Body에 대해 적절한 입력 검증을 수행할 것.
- 로그/Audit: 관리자 동작에 대해서는 감사 로그를 남기는 것이 권장됩니다.

- 보안 유의사항:
  - 관리자 토큰은 민감 정보므로 로그에 노출하면 안되며, 배포/운영 환경에서는 Secret Manager(예: Vault, K8s Secret) 사용을 권장합니다.
  - 쿠키는 `HttpOnly` 및 `Secure` (HTTPS 환경에서)로 설정되어야 합니다.

---

> 작업 시 브랜칭 할 것. 푸시 금지.
