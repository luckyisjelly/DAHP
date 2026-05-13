# 사용자 시나리오

> DAHP의 주요 사용 흐름. API 정합성 검증과 시연 스크립트의 기준.

작성일: 2026-05-13

---

## 1. 페르소나

### 1.1 김민준 (32세, 소유자 / Primary User)
- IT 회사 백엔드 개발자
- 클라우드 계정, 금융 계정, 개인 메모 등 다수 디지털 자산 보유
- 갑작스러운 사고/입원 등 비상 상황 대비 욕구

### 1.2 김지원 (30세, 수령인 / Recipient)
- 김민준의 배우자
- DAHP 회원이 아닌 외부 수령인
- 이메일/SMS로 안내 받음

### 1.3 박관리 (관리자 / Admin)
- DAHP 서비스 운영자
- 감사 로그 조회, 시스템 모니터링

---

## 2. 시나리오 1: 정상 가입~인계 플로우 (해피 패스)

### 목표
김민준이 가입부터 자산 인계까지 한 번에 완료.

### 단계

| # | 행위자 | 행동 | 시스템 응답 |
|---|---|---|---|
| 1 | 김민준 | `POST /api/auth/signup` (이메일, 비밀번호) | 201 Created, userId 반환 |
| 2 | 김민준 | `POST /api/auth/login` | 200 OK, accessToken/refreshToken |
| 3 | 김민준 | `POST /api/assets` ("Gmail 계정", ACCOUNT) | 201, assetId=1 |
| 4 | 김민준 | `POST /api/assets` ("은행 계좌 비밀번호 힌트", NOTE) | 201, assetId=2 |
| 5 | 김민준 | `POST /api/recipients` ("김지원", 이메일) | 201, recipientId=1 |
| 6 | 김민준 | `POST /api/handover-rules` (자산 1,2 + 수령인 1, conditionType=MANUAL_APPROVAL) | 201, ruleId=1, status=DRAFT |
| 7 | 김민준 | `POST /api/handover-rules/1/activate` | 200, status=ACTIVE |
| 8 | 김민준 | `POST /api/handover-rules/1/trigger` | 200, eventCount=2, events 목록 반환 |
| 9 | 시스템 | (콘솔에 알림 출력) | `[NOTIFY] 수령인 chulsoo@example.com에게 토큰 발송: <token>` |
| 10 | 김지원 | (콘솔에서 토큰 복사 후) `GET /api/handover-access/<token>` | 200, 자산 1 내용 반환 |
| 11 | 김지원 | (다른 토큰으로) `GET /api/handover-access/<token2>` | 200, 자산 2 내용 반환 |
| 12 | 김지원 | (같은 토큰 재시도) `GET /api/handover-access/<token>` | 410 `ACCESS_TOKEN_USED` |

### 검증 포인트
- ✅ 가입 후 즉시 로그인 가능
- ✅ 트리거 시 자산 수 × 수령인 수만큼 이벤트 생성
- ✅ 토큰 1회 사용 후 재사용 불가
- ✅ 수령인은 인증 없이 토큰만으로 자산 조회

---

## 3. 시나리오 2: 권한 검증 (보안)

### 목표
다른 사용자의 자산/수령인/규칙 접근 차단.

### 단계

| # | 행위자 | 행동 | 시스템 응답 |
|---|---|---|---|
| 1 | 김민준 | 자산 생성 (assetId=10) | 201 |
| 2 | 이공격 | (이공격 토큰으로) `GET /api/assets/10` | **403 FORBIDDEN_RESOURCE** |
| 3 | 이공격 | `DELETE /api/assets/10` | **403** |
| 4 | 이공격 | `POST /api/handover-rules` (assetIds=[10], recipientIds=[자기 수령인]) | **403** (남의 자산 포함) |
| 5 | 이공격 | (자기 자산만으로) 규칙 생성 → 자기 자산만 트리거 | 200 (자기 것이므로 OK) |
| 6 | 토큰 없이 | `GET /api/users/me` | **401** |

### 검증 포인트
- ✅ 다른 사용자 자산 직접 접근 403
- ✅ 규칙 생성 시 남의 자산/수령인 ID 끼워넣기 403
- ✅ 인증 없는 보호 API 401

---

## 4. 시나리오 3: 토큰 만료 / 비정상 케이스

### 목표
만료된 토큰, 잘못된 토큰, 취소된 이벤트 처리.

### 단계

| # | 행위자 | 행동 | 시스템 응답 |
|---|---|---|---|
| 1 | 김민준 | 규칙 트리거 → 토큰 발급 | 200 |
| 2 | (시간 경과 72h+) | | 시스템은 만료를 인지 |
| 3 | 김지원 | `GET /api/handover-access/<expired-token>` | **410 ACCESS_TOKEN_EXPIRED** |
| 4 | 누군가 | `GET /api/handover-access/aaaaaaaa-fake` | **404 INVALID_TOKEN** |
| 5 | 김민준 | (트리거 직후) `POST /api/handover-events/{id}/cancel` | 200, status=CANCELLED |
| 6 | 김지원 | `GET /api/handover-access/<cancelled-token>` | **410 ACCESS_TOKEN_USED** (또는 CANCELLED 별도 코드) |

### 검증 포인트
- ✅ 만료 토큰 410
- ✅ 잘못된 토큰 형식/존재하지 않는 토큰 404 (또는 401)
- ✅ 취소된 이벤트의 토큰 사용 불가

---

## 5. 시나리오 4: 체크인 (수동)

### 목표
사용자 체크인 갱신 및 상태 조회.

### 단계

| # | 행위자 | 행동 | 시스템 응답 |
|---|---|---|---|
| 1 | 김민준 | 가입 (`checkInIntervalDays`=30) | nextCheckInDueAt = createdAt + 30일 |
| 2 | 김민준 | `GET /api/check-ins/status` (가입 직후) | `isOverdue=false`, `daysUntilDue=30` |
| 3 | 김민준 | `POST /api/check-ins` | 200, `lastCheckInAt=now`, `nextCheckInDueAt=now+30d` |
| 4 | (29일 경과) | `GET /api/check-ins/status` | `daysUntilDue=1` |
| 5 | (31일 경과 / 미체크인) | `GET /api/check-ins/status` | `isOverdue=true` |
| 6 | 김민준 | `PATCH /api/users/me` { checkInIntervalDays: 14 } | 200 |
| 7 | 김민준 | `POST /api/check-ins` | `nextCheckInDueAt=now+14d` |

### 검증 포인트
- ✅ 체크인 주기 변경 즉시 반영
- ✅ overdue 정확히 판단
- ✅ MVP는 overdue가 자동 트리거를 발생시키지 않음 (수동 trigger만)

---

## 6. 시나리오 5: 시연용 통합 스크립트 (Sprint 4 발표)

### 목적
5분 안에 핵심 흐름 시연.

### 사전 준비
- DB 초기화 (`./gradlew flywayClean` 또는 수동)
- 시드 스크립트로 데모 사용자(`demo@dahp.com`) 미리 생성

### 시연 흐름 (각 단계 30초 이내)

1. **(15초)** Swagger UI 열기 → 가입 → 로그인 → 토큰 자동 인증
2. **(45초)** 자산 3개 등록 (계정 / 메모 / 링크)
3. **(20초)** 수령인 1명 등록
4. **(30초)** 규칙 생성 (자산 3 + 수령인 1, MANUAL_APPROVAL)
5. **(20초)** 규칙 활성화
6. **(30초)** **수동 트리거** → 이벤트 3개 생성, 콘솔에 토큰 3개 출력
7. **(45초)** 새 브라우저(시크릿 모드)에서 `/handover-access/<token>` 호출 → 자산 내용 노출
8. **(20초)** 같은 토큰 재호출 → 410 표시 (1회성 검증)
9. **(45초)** 만료 토큰 / 잘못된 토큰 케이스 (사전 발급된 만료 토큰 사용)
10. **(30초)** 감사 로그 조회 (ADMIN 로그인 후)

**총 ~5분**

---

## 7. 시나리오 외 (P2)

- 자동 조건 평가: INACTIVITY_PERIOD 30일 미체크인 → 시스템이 자동 트리거
- 실제 이메일 발송: 수령인에게 SMTP/SendGrid로 알림
- 수령인 OTP: 이메일로 6자리 코드 추가 인증
- 자산 파일 업로드: S3 presigned URL

이는 학기 후 또는 추가 개발에서 처리.

---

## 8. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 |
