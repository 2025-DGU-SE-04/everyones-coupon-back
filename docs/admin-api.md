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

## 공통 고려사항
- 인증: 모든 관리자 엔드포인트는 관리자 인증 토큰 또는 세션을 요구합니다.
- 권한 검증: 관리자 계정에 따른 권한 검증(예: 특정 게임 삭제 권한) 필요 시 추가 검증 필요.
- 입력 검증: Path/Body에 대해 적절한 입력 검증을 수행할 것.
- 로그/Audit: 관리자 동작에 대해서는 감사 로그를 남기는 것이 권장됩니다.

---

> 작업 시 브랜칭 할 것. 푸시 금지.
