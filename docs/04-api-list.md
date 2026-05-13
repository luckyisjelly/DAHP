# API 명세 (초안)

> 모든 REST 엔드포인트 목록. Sprint 2~3 구현의 기준.

작성일: 2026-05-13

---

## 1. 공통 사항

### 1.1 베이스 URL
- 로컬: `http://localhost:8080`
- 모든 API: `/api/...` 프리픽스

### 1.2 인증
- 인증 필요 API: `Authorization: Bearer <accessToken>` 헤더
- 공개 API: `/api/auth/**`, `/api/handover-access/**`, `/api/health`, `/swagger-ui/**`

### 1.3 응답 형식
모든 응답은 `ApiResponse<T>`로 래핑:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-05-13T09:37:00+09:00"
}
```

에러 응답:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ASSET_NOT_FOUND",
    "message": "자산을 찾을 수 없습니다."
  },
  "timestamp": "2026-05-13T09:37:00+09:00"
}
```

### 1.4 HTTP 상태 코드 정책
| 상태 | 의미 |
|---|---|
| 200 OK | 조회/업데이트 성공 |
| 201 Created | 생성 성공 |
| 204 No Content | 삭제 성공 |
| 400 Bad Request | 검증 실패 (DTO `@Valid`) |
| 401 Unauthorized | 토큰 없음/만료/잘못됨 |
| 403 Forbidden | 권한 없음 (소유자 불일치 등) |
| 404 Not Found | 리소스 없음 |
| 409 Conflict | 상태 전이 불가, 중복 등 |
| 500 Internal Server Error | 서버 오류 (운영 시에는 노출 최소화) |

### 1.5 페이징
목록 API는 다음 쿼리 파라미터 지원:
- `page` (기본 0)
- `size` (기본 20, 최대 100)
- `sort` (예: `createdAt,desc`)

페이징 응답:
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

---

## 2. 엔드포인트 목록 (요약)

| Sprint | 도메인 | Method | Path | 인증 | 설명 |
|---|---|---|---|---|---|
| 2 | health | GET | `/api/health` | X | 헬스 체크 |
| 2 | auth | POST | `/api/auth/signup` | X | 회원 가입 |
| 2 | auth | POST | `/api/auth/login` | X | 로그인 |
| 2 | auth | POST | `/api/auth/refresh` | X | 토큰 갱신 |
| 2 | auth | POST | `/api/auth/logout` | O | 로그아웃 |
| 2 | user | GET | `/api/users/me` | O | 내 정보 조회 |
| 2 | user | PATCH | `/api/users/me` | O | 내 정보 수정 |
| 2 | asset | POST | `/api/assets` | O | 자산 생성 |
| 2 | asset | GET | `/api/assets` | O | 자산 목록 |
| 2 | asset | GET | `/api/assets/{id}` | O | 자산 상세 |
| 2 | asset | PATCH | `/api/assets/{id}` | O | 자산 수정 |
| 2 | asset | DELETE | `/api/assets/{id}` | O | 자산 삭제 |
| 3 | recipient | POST | `/api/recipients` | O | 수령인 등록 |
| 3 | recipient | GET | `/api/recipients` | O | 수령인 목록 |
| 3 | recipient | GET | `/api/recipients/{id}` | O | 수령인 상세 |
| 3 | recipient | PATCH | `/api/recipients/{id}` | O | 수령인 수정 |
| 3 | recipient | DELETE | `/api/recipients/{id}` | O | 수령인 삭제 |
| 3 | rule | POST | `/api/handover-rules` | O | 규칙 생성 |
| 3 | rule | GET | `/api/handover-rules` | O | 규칙 목록 |
| 3 | rule | GET | `/api/handover-rules/{id}` | O | 규칙 상세 |
| 3 | rule | PATCH | `/api/handover-rules/{id}` | O | 규칙 수정 |
| 3 | rule | DELETE | `/api/handover-rules/{id}` | O | 규칙 삭제 |
| 3 | rule | POST | `/api/handover-rules/{id}/activate` | O | 규칙 활성화 |
| 3 | rule | POST | `/api/handover-rules/{id}/pause` | O | 규칙 일시정지 |
| 3 | rule | POST | `/api/handover-rules/{id}/trigger` | O | 수동 트리거 |
| 3 | event | GET | `/api/handover-events` | O | 내가 발생시킨 이벤트 목록 |
| 3 | event | GET | `/api/handover-events/{id}` | O | 이벤트 상세 |
| 3 | event | POST | `/api/handover-events/{id}/cancel` | O | 이벤트 취소 |
| 3 | access | GET | `/api/handover-access/{token}` | X | 수령인 자산 조회 (1회) |
| 3/4 | checkin | POST | `/api/check-ins` | O | 수동 체크인 |
| 3/4 | checkin | GET | `/api/check-ins/status` | O | 체크인 상태 |
| 4 | audit | GET | `/api/admin/audit-logs` | O (ADMIN) | 감사 로그 |

**총 28개 엔드포인트** (헬스 체크 포함).

---

## 3. 상세 명세

### 3.1 Auth

#### POST `/api/auth/signup`
회원 가입.

**Request**
```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!",
  "checkInIntervalDays": 30
}
```

검증: `email` 형식, `password` 최소 8자, `checkInIntervalDays` 1~365.

**Response 201**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "user@example.com"
  }
}
```

**에러**
- 409 `EMAIL_ALREADY_EXISTS`

---

#### POST `/api/auth/login`

**Request**
```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!"
}
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 3600
  }
}
```

**에러**
- 401 `LOGIN_FAILED` (이메일/비밀번호 불일치 — 어느 쪽인지 노출 X)

---

#### POST `/api/auth/refresh`

**Request**
```json
{ "refreshToken": "eyJ..." }
```

**Response 200**: `LoginResponse`와 동일.

---

### 3.2 User

#### GET `/api/users/me`

**Response 200**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "role": "USER",
    "checkInIntervalDays": 30,
    "lastCheckInAt": "2026-05-13T10:00:00+09:00",
    "nextCheckInDueAt": "2026-06-12T10:00:00+09:00",
    "createdAt": "2026-05-01T00:00:00+09:00"
  }
}
```

#### PATCH `/api/users/me`
**Request** (모두 옵셔널)
```json
{ "checkInIntervalDays": 14 }
```

---

### 3.3 Asset

#### POST `/api/assets`
**Request**
```json
{
  "title": "Gmail 계정",
  "type": "ACCOUNT",
  "description": "주 이메일 계정",
  "content": "ID: user@gmail.com, hint: ...",
  "externalRef": null,
  "sensitivityLevel": "HIGH"
}
```

**Response 201**: `AssetResponse`.

#### GET `/api/assets?type=ACCOUNT&q=Gmail&page=0&size=20&sort=createdAt,desc`
**Response 200**: 페이징된 `AssetResponse` 목록.

#### GET `/api/assets/{id}`
**Response 200**: `AssetResponse`.
**에러**: 404 `ASSET_NOT_FOUND`, 403 `FORBIDDEN`.

#### PATCH `/api/assets/{id}`
부분 수정. null은 무시.

#### DELETE `/api/assets/{id}`
**Response 204**.

**AssetResponse 예시**
```json
{
  "id": 1,
  "title": "Gmail 계정",
  "type": "ACCOUNT",
  "description": "주 이메일 계정",
  "content": "ID: ...",
  "externalRef": null,
  "sensitivityLevel": "HIGH",
  "createdAt": "2026-05-13T10:00:00+09:00",
  "updatedAt": "2026-05-13T10:00:00+09:00"
}
```

---

### 3.4 Recipient

#### POST `/api/recipients`
```json
{
  "name": "김철수",
  "email": "chulsoo@example.com",
  "phone": "010-1234-5678",
  "relationship": "형제",
  "memo": "장남"
}
```

**Response 201**: `RecipientResponse`.

#### GET/PATCH/DELETE: 동일 패턴.

---

### 3.5 HandoverRule

#### POST `/api/handover-rules`
```json
{
  "title": "사망 시 가족에게 인계",
  "description": "...",
  "conditionType": "MANUAL_APPROVAL",
  "conditionValue": null,
  "assetIds": [1, 2, 3],
  "recipientIds": [1, 2]
}
```

검증:
- `assetIds`의 모든 자산 소유자 = 현재 사용자
- `recipientIds`의 모든 수령인 소유자 = 현재 사용자
- 위반 시 403 `FORBIDDEN_RESOURCE`

**Response 201**: `HandoverRuleResponse`.

#### GET `/api/handover-rules` / `/api/handover-rules/{id}`
**HandoverRuleResponse 예시**
```json
{
  "id": 1,
  "title": "사망 시 가족에게 인계",
  "description": "...",
  "conditionType": "MANUAL_APPROVAL",
  "conditionValue": null,
  "status": "DRAFT",
  "assets": [
    { "id": 1, "title": "Gmail 계정", "type": "ACCOUNT" }
  ],
  "recipients": [
    { "id": 1, "name": "김철수", "email": "chulsoo@example.com" }
  ],
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### POST `/api/handover-rules/{id}/activate`
- 전제: `status` ∈ {`DRAFT`, `PAUSED`}
- 후효: `status` = `ACTIVE`
- 위반 시 409 `INVALID_STATE_TRANSITION`

#### POST `/api/handover-rules/{id}/pause`
- 전제: `status` = `ACTIVE`
- 후효: `status` = `PAUSED`

#### POST `/api/handover-rules/{id}/trigger`
**수동 트리거 (MVP 핵심).**
- 전제: `status` = `ACTIVE`
- 후효:
  - `status` = `TRIGGERED`
  - 각 자산 × 수령인 조합으로 `HandoverEvent` 생성
  - 알림 발송 (콘솔)
- 토큰 원본은 응답에 노출 X. 알림 메시지에 포함.

**Response 200**
```json
{
  "success": true,
  "data": {
    "ruleId": 1,
    "ruleStatus": "TRIGGERED",
    "eventCount": 6,
    "events": [
      { "eventId": 10, "assetId": 1, "recipientId": 1, "expiresAt": "..." },
      ...
    ]
  }
}
```

---

### 3.6 HandoverEvent

#### GET `/api/handover-events`
내가 소유한 규칙에서 발생한 이벤트 목록.

#### POST `/api/handover-events/{id}/cancel`
- 전제: `status` = `PENDING` 또는 `NOTIFIED`
- 후효: `status` = `CANCELLED`

---

### 3.7 HandoverAccess (수령인용, 공개)

#### GET `/api/handover-access/{token}`
**인증 없음.** 토큰만으로 자산 1회 조회.

**Response 200**
```json
{
  "success": true,
  "data": {
    "asset": {
      "title": "Gmail 계정",
      "type": "ACCOUNT",
      "description": "주 이메일 계정",
      "content": "ID: user@gmail.com, hint: ...",
      "externalRef": null
    },
    "rule": {
      "title": "사망 시 가족에게 인계"
    },
    "owner": {
      "email": "user@example.com"
    },
    "expiresAt": "...",
    "accessedAt": "2026-05-13T10:30:00+09:00"
  }
}
```

**에러**
- 404 `INVALID_TOKEN` (해시 매칭 실패)
- 410 Gone `TOKEN_EXPIRED`
- 410 Gone `TOKEN_ALREADY_USED`

**보안 노트**: 응답 후 `status` = `ACCESSED`, `accessedAt` = 현재. 재호출 시 410.

---

### 3.8 CheckIn

#### POST `/api/check-ins`
**Request**: 본문 없음 (또는 빈 객체).

**Response 200**
```json
{
  "success": true,
  "data": {
    "lastCheckInAt": "2026-05-13T10:00:00+09:00",
    "nextCheckInDueAt": "2026-06-12T10:00:00+09:00"
  }
}
```

#### GET `/api/check-ins/status`
```json
{
  "success": true,
  "data": {
    "lastCheckInAt": "...",
    "nextCheckInDueAt": "...",
    "isOverdue": false,
    "daysUntilDue": 23
  }
}
```

---

### 3.9 AuditLog (Admin)

#### GET `/api/admin/audit-logs?actorUserId=&actionType=&from=&to=&page=&size=`
ADMIN 권한만.

---

## 4. 에러 코드 (ErrorCode enum)

| 코드 | HTTP | 설명 |
|---|---|---|
| `EMAIL_ALREADY_EXISTS` | 409 | 가입 시 이메일 중복 |
| `LOGIN_FAILED` | 401 | 로그인 실패 |
| `INVALID_TOKEN` | 401 | JWT 또는 access token 검증 실패 |
| `TOKEN_EXPIRED` | 401 | JWT 만료 |
| `ACCESS_TOKEN_EXPIRED` | 410 | 수령인 access token 만료 |
| `ACCESS_TOKEN_USED` | 410 | 수령인 access token 이미 사용 |
| `USER_NOT_FOUND` | 404 | |
| `ASSET_NOT_FOUND` | 404 | |
| `RECIPIENT_NOT_FOUND` | 404 | |
| `RULE_NOT_FOUND` | 404 | |
| `EVENT_NOT_FOUND` | 404 | |
| `FORBIDDEN_RESOURCE` | 403 | 소유자 불일치 |
| `INVALID_STATE_TRANSITION` | 409 | 상태 전이 불가 |
| `VALIDATION_FAILED` | 400 | DTO 검증 실패 |
| `INTERNAL_ERROR` | 500 | 서버 오류 |

---

## 5. Swagger
- 경로: `/swagger-ui/index.html`
- 보안 스키마: Bearer JWT (`@SecurityScheme`)
- 각 컨트롤러에 `@Tag`, 각 메서드에 `@Operation`, `@ApiResponse` 적용

---

## 6. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 |
