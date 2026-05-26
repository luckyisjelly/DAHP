# DAHP 백엔드 API 명세 (프론트엔드용)

> 프론트엔드 작업을 위한 백엔드 API 상세 명세 + 와이어프레임 갭 분석.
> Auth/User/ApiResponse는 이미 확인 완료 → 스킵.
> 대상: Asset / Recipient / HandoverRule / HandoverEvent / HandoverAccess / CheckIn / HandoverEvaluation

작성일: 2026-05-26
백엔드 베이스 URL: `http://localhost:8090`
Swagger UI: http://localhost:8090/swagger-ui/index.html

---

## 0. 공통 사항 (요약)

### 인증
- 인증 필요한 모든 API: `Authorization: Bearer <accessToken>` 헤더
- 공개 엔드포인트: `/api/auth/**`, `/api/handover-access/**`, `/api/health`, `/swagger-ui/**`, `/v3/api-docs/**`

### CORS
- 허용 origin: `http://localhost:5173`, `http://localhost:3000`
- 노출 헤더: `Authorization`, `Location`
- 자격 증명 허용 (credentials: true)

### 응답 포맷 (`ApiResponse<T>`)
```ts
{
  success: boolean,
  message?: string,         // 한글 친절 메시지 (선택)
  data: T | null,
  error: {
    code: string,           // ErrorCode enum 이름
    message: string,
    hint?: string           // 사용자 도움말 (선택)
  } | null,
  timestamp: string         // ISO-8601 OffsetDateTime, +09:00
}
```
- `@JsonInclude(NON_NULL)` 적용 → null 필드는 응답 JSON에서 제외됨.

### 페이징 응답 (`PageResponse<T>`)
```ts
{
  content: T[],
  page: number,
  size: number,
  totalElements: number,
  totalPages: number,
  first: boolean,
  last: boolean
}
```

### 페이징 요청 파라미터 (Spring Pageable)
- `page` (기본 0, 0-indexed)
- `size` (기본 20, 컨트롤러별 다를 수 있음)
- `sort` (예: `createdAt,desc`, 다중 정렬 가능)

### 글로벌 ErrorCode (관련 일부)
| code | HTTP | 의미 |
|---|---|---|
| `VALIDATION_FAILED` | 400 | DTO 검증 실패 (필드별 상세 메시지) |
| `FORBIDDEN_RESOURCE` | 403 | 소유자 불일치 (IDOR 차단) |
| `INTERNAL_ERROR` | 500 | 알 수 없는 서버 오류 |

---

## 1. Asset (디지털 자산)

### Enums

#### `AssetType` (자산 유형)
```
ACCOUNT     - 계정 정보 (예: Gmail 계정)
FILE        - 파일 (URL/경로 텍스트로만 저장)
NOTE        - 메모/유서 텍스트
LINK        - 외부 링크
MESSAGE     - 메시지
DOCUMENT    - 문서
ETC         - 기타
```

#### `SensitivityLevel` (민감도)
```
LOW
MEDIUM      - 기본값
HIGH
```

### Asset ErrorCode
| code | HTTP | 메시지 | hint |
|---|---|---|---|
| `ASSET_NOT_FOUND` | 404 | 자산을 찾을 수 없습니다. | 이미 삭제되었거나 본인 소유가 아닌 자산일 수 있습니다. |
| `FORBIDDEN_RESOURCE` | 403 | 해당 리소스에 접근할 권한이 없습니다. | 본인이 소유한 리소스만 조회/수정할 수 있습니다. |
| `VALIDATION_FAILED` | 400 | 요청 검증에 실패했습니다. | 요청 본문의 필수 필드와 형식을 다시 확인해주세요. |

---

### 1.1 `POST /api/assets` — 자산 생성

**인증**: 필수

**Request Body**: `AssetCreateRequest`
| 필드 | 타입 | 필수 | 검증 | 비고 |
|---|---|---|---|---|
| `title` | String | ✅ `@NotBlank` | `@Size(max=200)` | 제목 |
| `type` | `AssetType` | ✅ `@NotNull` | enum | 위 7개 값 |
| `description` | String | ❌ | `@Size(max=1000)` | |
| `content` | String | ❌ | 검증 없음 | TEXT 컬럼, 평문 저장 (암호화 미구현, 아래 갭 참고) |
| `externalRef` | String | ❌ | `@Size(max=500)` | 외부 URL/참조 경로 |
| `sensitivityLevel` | `SensitivityLevel` | ❌ | enum, 기본 `MEDIUM` | |

**Response 201 Created**: `ApiResponse<AssetResponse>`
- `message`: `"자산 '{title}'이(가) 등록되었습니다. (id={id})"`

`AssetResponse`:
| 필드 | 타입 | nullable |
|---|---|---|
| `id` | Long | X |
| `title` | String | X |
| `type` | `AssetType` | X |
| `description` | String | O |
| `content` | String | O (NoOp 암호화라 평문 반환) |
| `externalRef` | String | O |
| `sensitivityLevel` | `SensitivityLevel` | X |
| `createdAt` | LocalDateTime (`YYYY-MM-DDTHH:mm:ss.SSSSSS`) | X |
| `updatedAt` | LocalDateTime | X |

**에러**
- 400 `VALIDATION_FAILED`

**비즈니스 규칙**
- 생성 시 ownerId = 인증된 사용자 ID 자동 주입 (요청 본문에 owner 받지 않음)
- `content` 필드는 `EncryptionService` 인터페이스를 통해 처리되지만 MVP는 `NoOpEncryptionService` (입력 그대로 저장). `contentEncrypted` 컬럼은 항상 `false`.
- `sensitivityLevel` 누락 시 엔티티 기본값 `MEDIUM`

> **갭 분석 (와이어프레임 vs 백엔드)**
> - ⚠️ `유형 (계정/파일/메시지)` — `AssetType` enum이 7개로 더 풍부함. 프론트 매핑: 계정=ACCOUNT, 파일=FILE, 메시지=MESSAGE. NOTE/LINK/DOCUMENT/ETC도 UI에 노출할지 결정 필요. 줄이려면 백엔드 enum 축소 필요(마이그레이션).
> - ❌ `카테고리` 필드 없음 — 추가하려면 `category` String 또는 enum 신설 필요. 임시 대안: `description` 머리에 카테고리 prefix 박기.
> - ✅ `URL` — `externalRef` (String 500자)로 사용 가능. 다만 형식 검증 없음 (`@URL` 미적용).
> - ❌ `사용자명` 필드 없음 — ACCOUNT 타입 필수일 텐데 별도 컬럼 없음. 대안 1: `content`에 JSON으로 포함. 대안 2: 백엔드에 `accountUsername` 필드 추가.
> - ⚠️ `비밀번호` — `content` 필드(TEXT, 평문)에 저장하는 게 현재 패턴. **실 암호화 미구현** (`NoOpEncryptionService`). 인터페이스 `EncryptionService`는 이미 추상화돼있어 P2에서 AES-GCM 구현체로 교체 가능. 프론트에선 일반 텍스트 입력 처리, "백엔드 평문 저장 중" 경고 노출 권장.
> - ✅ `설명` — `description` (String 1000자).
> - ✅ `중요도` — `SensitivityLevel` enum (LOW/MEDIUM/HIGH) 정확히 일치.
> - ❌ `태그 (string array)` 없음 — 추가하려면 `List<String> tags` 또는 별도 `asset_tags` 테이블 신설 필요. JPA `@ElementCollection`이 가장 단순.
> - ⚠️ `연결된 수신자 N명` — Asset 자체엔 없음. 해당 자산을 포함한 `HandoverRule`을 통해 간접 연결됨. 프론트에서 Asset 상세 화면에 수신자 표시하려면 별도 API (예: `GET /api/assets/{id}/recipients` 또는 `GET /api/handover-rules?assetId=` 필터) 신설 필요.

---

### 1.2 `GET /api/assets` — 자산 목록 (페이징/필터)

**인증**: 필수

**Query Params**
| 이름 | 타입 | 필수 | 비고 |
|---|---|---|---|
| `type` | `AssetType` | ❌ | 자산 타입 필터 |
| `q` | String | ❌ | 제목 부분 일치 (LOWER LIKE %q%) |
| `page` | int | ❌ | 기본 0 |
| `size` | int | ❌ | 기본 20 |
| `sort` | String | ❌ | 기본 `createdAt,desc` |

**Response 200**: `ApiResponse<PageResponse<AssetResponse>>`
- `message`: `"총 N건 중 X~Y번째를 반환합니다."`

**비즈니스 규칙**
- 본인 자산만 조회 (다른 사용자 것 격리)
- 4가지 조합 분기 메서드로 처리 (type+q / type / q / 없음)

---

### 1.3 `GET /api/assets/{id}` — 자산 상세

**Path Params**: `id` (Long)
**Response 200**: `ApiResponse<AssetResponse>`
**에러**: 404 `ASSET_NOT_FOUND`, 403 `FORBIDDEN_RESOURCE` (타인 자산)

---

### 1.4 `PATCH /api/assets/{id}` — 자산 부분 수정

**Request Body**: `AssetUpdateRequest` (모든 필드 옵셔널, null이면 무시)
| 필드 | 타입 | 검증 |
|---|---|---|
| `title` | String | `@Size(max=200)` |
| `type` | `AssetType` | |
| `description` | String | `@Size(max=1000)` |
| `content` | String | |
| `externalRef` | String | `@Size(max=500)` |
| `sensitivityLevel` | `SensitivityLevel` | |

**Response 200**: `ApiResponse<AssetResponse>` (`message: "자산 #{id}이(가) 수정되었습니다."`)
**에러**: 404, 403, 400

---

### 1.5 `DELETE /api/assets/{id}` — 자산 삭제

**Response 200**: `ApiResponse<Void>` (`message: "자산 #{id}이(가) 삭제되었습니다."`)
- ⚠️ 204 No Content 아니고 **200 + 메시지 본문**임 (UX 강화 시 변경됨)
**에러**: 404, 403

---

## 2. Recipient (수령인)

### Enums
없음. `relationship`은 자유 String.

### Recipient ErrorCode
| code | HTTP | 메시지 |
|---|---|---|
| `RECIPIENT_NOT_FOUND` | 404 | 수령인을 찾을 수 없습니다. |
| `FORBIDDEN_RESOURCE` | 403 | 해당 리소스에 접근할 권한이 없습니다. |

---

### 2.1 `POST /api/recipients` — 수령인 등록

**Request Body**: `RecipientCreateRequest`
| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| `name` | String | ✅ `@NotBlank` | `@Size(max=100)` |
| `email` | String | ✅ `@NotBlank` `@Email` | `@Size(max=255)` |
| `phone` | String | ❌ | `@Size(max=30)` |
| `relationship` | String | ❌ | `@Size(max=50)` (자유 텍스트) |
| `memo` | String | ❌ | `@Size(max=500)` |

**Response 201**: `ApiResponse<RecipientResponse>`
- `message`: `"수령인 '{name}'이(가) 등록되었습니다. (id={id})"`

`RecipientResponse`:
| 필드 | 타입 | nullable |
|---|---|---|
| `id` | Long | X |
| `name` | String | X |
| `email` | String | X |
| `phone` | String | O |
| `relationship` | String | O |
| `memo` | String | O |
| `createdAt` | LocalDateTime | X |
| `updatedAt` | LocalDateTime | X |

---

### 2.2 `GET /api/recipients` — 수령인 목록

**Query Params**
| 이름 | 타입 | 비고 |
|---|---|---|
| `q` | String | 이름 부분 일치 |
| `page`/`size`/`sort` | | 기본 size=20, sort=createdAt,desc |

**Response 200**: `ApiResponse<PageResponse<RecipientResponse>>` (`message: "수령인 총 N명."`)

---

### 2.3 `GET /api/recipients/{id}` — 수령인 상세
**Response 200**: `ApiResponse<RecipientResponse>`
**에러**: 404 `RECIPIENT_NOT_FOUND`, 403

### 2.4 `PATCH /api/recipients/{id}` — 부분 수정
**Request Body**: `RecipientUpdateRequest` (모든 필드 옵셔널, `email`은 형식 검증)
**Response 200**: `ApiResponse<RecipientResponse>` (`message: "수령인 #{id}이(가) 수정되었습니다."`)

### 2.5 `DELETE /api/recipients/{id}` — 삭제
**Response 200**: `ApiResponse<Void>` (200 + message)

---

> **갭 분석 (Recipient)**
> - ✅ `이름`, `이메일`, `전화번호` — 모두 일치 (name/email/phone).
> - ⚠️ `관계 (가족/친구/동료/기타)` — 백엔드는 String 50자 자유 텍스트. enum 아님. 프론트가 강제하려면 백엔드 enum 신설 필요 (`RelationshipType`). 임시: 프론트가 입력 시 미리 정의된 옵션 + "기타" 직접 입력으로 처리.
> - ❌ `인증 방법 (이메일/SMS/이중 인증)` — 백엔드 없음. MVP는 토큰만으로 접근 (`/api/handover-access/{token}`). SMS/이중인증 추가하려면 `Recipient.authMethod`, `Recipient.requireTwoFactor` 컬럼 + 인증 흐름 신설 필요 (P2 큰 작업).
> - ❌ `접근 권한 (열람 전용 / 전체 제어)` — 백엔드 없음. 현재 토큰은 1회 열람 전용. "전체 제어" 의미를 정의해야 함 (다운로드? 수정? 일반적으론 다운로드 가능 정도).
> - ❌ `2단계 인증 필요 여부` — 백엔드 없음. 위 인증 방법과 함께 처리.

---

## 3. HandoverRule (인계 규칙)

### Enums

#### `HandoverConditionType` (트리거 조건)
```
MANUAL_APPROVAL     - 소유자가 수동 trigger API 호출 시 발동
SPECIFIC_DATE       - conditionValue(ISO 날짜)가 지나면 자동 트리거
INACTIVITY_PERIOD   - 소유자 lastCheckInAt이 conditionValue(일수) 이상 경과 시 자동 트리거
```

#### `HandoverRuleStatus` (규칙 상태)
```
DRAFT       - 생성 직후, 평가 대상 아님
ACTIVE      - 활성화됨, 평가/트리거 대상
PAUSED      - 일시 정지
TRIGGERED   - 트리거 발생, 이벤트 생성됨
COMPLETED   - 모든 이벤트 종료
CANCELLED   - 명시적 취소
```

상태 전이 (도메인 메서드):
- `activate()`: DRAFT/PAUSED → ACTIVE (다른 상태에선 `INVALID_STATE_TRANSITION` 409)
- `pause()`: ACTIVE → PAUSED만
- `markTriggered()`: ACTIVE → TRIGGERED만
- `cancel()`: 어떤 상태에서든 CANCELLED로 (단, 위 도메인 메서드는 호출 시점 검증)

### HandoverRule ErrorCode
| code | HTTP | 메시지 |
|---|---|---|
| `RULE_NOT_FOUND` | 404 | 인계 규칙을 찾을 수 없습니다. |
| `FORBIDDEN_RESOURCE` | 403 | 해당 리소스에 접근할 권한이 없습니다. |
| `INVALID_STATE_TRANSITION` | 409 | 허용되지 않은 상태 전이입니다. (예: `현재 상태 PAUSED에서 PAUSED(으)로 전이할 수 없습니다.`) |

---

### 3.1 `POST /api/handover-rules` — 규칙 생성 (DRAFT)

**Request Body**: `HandoverRuleCreateRequest`
| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| `title` | String | ✅ `@NotBlank` | `@Size(max=200)` |
| `description` | String | ❌ | (TEXT, 길이 무제한) |
| `conditionType` | `HandoverConditionType` | ✅ `@NotNull` | enum |
| `conditionValue` | String | ❌ | `@Size(max=500)` (날짜 ISO / 일수 등) |
| `assetIds` | `List<Long>` | ✅ `@NotEmpty` | 최소 1개 |
| `recipientIds` | `List<Long>` | ✅ `@NotEmpty` | 최소 1개 |

`conditionValue` 포맷 (조건 타입별):
- `MANUAL_APPROVAL`: null (사용 안 함)
- `SPECIFIC_DATE`: ISO LocalDate (`YYYY-MM-DD`, 예: `"2026-12-31"`)
- `INACTIVITY_PERIOD`: 정수 일수 (예: `"30"`)
- 파싱 실패 시 스케줄러가 평가 skip + warn 로그. 트리거 안 됨.

**Response 201**: `ApiResponse<HandoverRuleResponse>`
- `message`: `"인계 규칙 '{title}'이(가) DRAFT 상태로 생성되었습니다. activate API로 활성화하세요."`

`HandoverRuleResponse`:
| 필드 | 타입 | nullable |
|---|---|---|
| `id` | Long | X |
| `title` | String | X |
| `description` | String | O |
| `conditionType` | `HandoverConditionType` | X |
| `conditionValue` | String | O |
| `status` | `HandoverRuleStatus` | X |
| `assets` | `AssetSummary[]` | X (빈 배열 가능) |
| `recipients` | `RecipientSummary[]` | X |
| `createdAt` | LocalDateTime | X |
| `updatedAt` | LocalDateTime | X |

`AssetSummary`: `{ id: Long, title: string, type: AssetType }`
`RecipientSummary`: `{ id: Long, name: string, email: string }`

**에러**
- 400 `VALIDATION_FAILED` (assetIds/recipientIds 비어있음, title 누락 등)
- 403 `FORBIDDEN_RESOURCE`: assetIds/recipientIds 중 본인 소유 아닌 것 포함 시 (IDOR 차단)

**비즈니스 규칙**
- 생성 직후 status는 항상 `DRAFT`
- 교차 검증: 모든 assetIds/recipientIds가 본인 소유여야 함. 하나라도 타인 것이면 403
- M:N 매핑 테이블 `handover_rule_assets`, `handover_rule_recipients`에 별도 row로 저장 (`@ManyToMany` 사용 X)

---

### 3.2 `GET /api/handover-rules` — 목록 (상태 필터)

**Query Params**
| 이름 | 타입 | 비고 |
|---|---|---|
| `status` | `HandoverRuleStatus` | ❌ |
| `page`/`size`/`sort` | | 기본 size=20, sort=createdAt,desc |

**Response 200**: `ApiResponse<PageResponse<HandoverRuleResponse>>`

---

### 3.3 `GET /api/handover-rules/{id}` — 상세 (포함된 자산/수신자 같이)
**Response 200**: `ApiResponse<HandoverRuleResponse>`

### 3.4 `PATCH /api/handover-rules/{id}` — 부분 수정
**Request Body**: `HandoverRuleUpdateRequest` (모든 필드 옵셔널)
- `title`, `description`, `conditionType`, `conditionValue`: null이면 무시
- `assetIds`, `recipientIds`: **null이면 변경 없음, 비어있지 않은 리스트면 전체 교체** (`delete + saveAll`)

**Response 200**: `ApiResponse<HandoverRuleResponse>`

### 3.5 `DELETE /api/handover-rules/{id}` — 삭제
**Response 200**: `ApiResponse<Void>` (M:N 매핑 행도 함께 삭제)

### 3.6 `POST /api/handover-rules/{id}/activate` — 활성화
**Request Body**: 없음 (또는 빈 JSON)
**Response 200**: `ApiResponse<HandoverRuleResponse>` (`message: "인계 규칙 #{id}이(가) 활성화되었습니다."`)
**에러**: 409 `INVALID_STATE_TRANSITION` (DRAFT/PAUSED 외 상태에서 호출 시)

### 3.7 `POST /api/handover-rules/{id}/pause` — 일시정지
**Request Body**: 없음
**Response 200**: `ApiResponse<HandoverRuleResponse>` (`message: "인계 규칙 #{id}이(가) 일시정지되었습니다."`)
**에러**: 409 (ACTIVE 외 상태에서 호출 시)

### 3.8 `POST /api/handover-rules/{id}/trigger` — 수동 트리거 (핵심)
**Request Body**: 없음
**Response 200**: `ApiResponse<HandoverTriggerResponse>`
- `message`: `"인계 규칙 #{id} 트리거됨. {N}개의 인계 이벤트가 생성되어 콘솔(서버 로그)에 알림이 출력되었습니다. 토큰은 로그에서 확인 후 GET /api/handover-access/{token} 으로 접근하세요."`

`HandoverTriggerResponse`:
| 필드 | 타입 |
|---|---|
| `ruleId` | Long |
| `ruleStatus` | `HandoverRuleStatus` (=TRIGGERED) |
| `eventCount` | int |
| `events` | `HandoverEventResponse[]` |

**에러**: 409 (ACTIVE 외에서 호출 시)

**비즈니스 규칙**
- ACTIVE → TRIGGERED 전이 후, 자산×수령인 cross-product로 `HandoverEvent` N개 생성
- 각 이벤트에 secure random 토큰 발급, **DB는 SHA-256 해시만** 저장
- 원본 토큰은 알림(콘솔)에만 노출 → **응답 body에는 raw token 없음** (보안)
- 알림 발송 후 각 이벤트 상태 PENDING → NOTIFIED

> **갭 분석 (HandoverRule)**
> - ✅ `규칙 이름` — `title`.
> - ⚠️ `트리거 타입 (미접속/날짜/수동)` — `HandoverConditionType` enum (3개) 일치. 와이어프레임 명칭 ↔ enum 매핑: 미접속=INACTIVITY_PERIOD, 날짜=SPECIFIC_DATE, 수동=MANUAL_APPROVAL.
> - ⚠️ `미접속 기간 (7~365일)` — `conditionValue` String (max 500자)으로 들어감. **숫자 범위 검증 없음**. 프론트에서 7~365 강제하거나 백엔드 검증 추가 필요. 타입 안전성 떨어짐 (`conditionValue`가 String이라 INACTIVITY 일수도 String).
> - ❌ `유예기간 (7일)` 없음 — 트리거 즉시 이벤트 발생. cancel은 이벤트 상태(PENDING/NOTIFIED)에서 항상 가능, 시간 제한 X. "유예기간" 개념 도입하려면 `HandoverRule.gracePeriodDays` 컬럼 신설 + 트리거 → 즉시 이벤트 생성 X, gracePeriod 후 자동 NOTIFIED + 알림 발송 로직 필요.
> - ❌ `사전 알림 시점 (N일 전, 다중)` 없음 — 트리거 시 1회 알림만. 다단계 알림 스케줄링 미구현 (P2 큰 작업).
> - ❌ `알림 방법 (이메일/SMS/푸시)` 없음 — `NotificationService` 인터페이스 + `ConsoleNotificationService` 단일 구현체. `NotificationType` enum은 존재(HANDOVER_TRIGGERED, CHECK_IN_REMINDER, ACCESS_TOKEN_EXPIRING). 실 채널 추가 시 구현체 확장 필요.
> - ❌ `Escalation (단계별 확대)` 없음 — 단계별 알림 + 미응답 시 다음 대상자로 확대하는 워크플로우 미구현.
> - ❌ `사용자/수신자 알림 토글` 없음 — 현재 알림은 수신자에게만 (소유자에게 trigger 결과는 응답으로 반환됨).
> - ✅ `상태 (DRAFT/ACTIVE/PAUSED)` — `HandoverRuleStatus`에 더 많은 상태 있음(TRIGGERED/COMPLETED/CANCELLED). 프론트는 그 추가 상태도 표시해야 정확.
> - ✅ `연결된 자산 IDs / 수신자 IDs` — Create/Update 모두 지원.

---

## 4. HandoverEvent (트리거된 인계 이벤트, 소유자 시점)

### Enums

#### `HandoverEventStatus`
```
PENDING     - 이벤트 생성, 알림 발송 전
NOTIFIED    - 수령인에게 알림 발송 완료
ACCESSED    - 수령인이 토큰으로 접근 완료 (1회 사용 끝)
EXPIRED     - 미접근으로 만료 (스케줄러가 자동 전이)
CANCELLED   - 소유자가 취소
```

상태 전이 (도메인 메서드):
- `markNotified()`: PENDING → NOTIFIED (다른 상태 무시)
- `markAccessed()`: 검증(만료/사용/취소 체크) → ACCESSED
- `cancel()`: PENDING 또는 NOTIFIED만 가능, 아니면 `INVALID_STATE_TRANSITION` 409
- `markExpired()`: PENDING/NOTIFIED → EXPIRED (스케줄러용, 다른 상태 무시)

### Event ErrorCode
| code | HTTP |
|---|---|
| `EVENT_NOT_FOUND` | 404 |
| `FORBIDDEN_RESOURCE` | 403 |
| `INVALID_STATE_TRANSITION` | 409 (ACCESSED/EXPIRED 등 이벤트 cancel 시도 시) |

---

### 4.1 `GET /api/handover-events` — 내 이벤트 목록

**Query Params**
| 이름 | 타입 | 비고 |
|---|---|---|
| `status` | `HandoverEventStatus` | ❌ |
| `page`/`size`/`sort` | | 기본 size=20, sort=triggeredAt,desc |

**Response 200**: `ApiResponse<PageResponse<HandoverEventResponse>>` (`message: "인계 이벤트 총 N건."`)

`HandoverEventResponse`:
| 필드 | 타입 | nullable |
|---|---|---|
| `id` | Long | X |
| `ruleId` | Long | X |
| `assetId` | Long | X |
| `recipientId` | Long | X |
| `status` | `HandoverEventStatus` | X |
| `triggeredAt` | LocalDateTime | X |
| `expiresAt` | LocalDateTime | X (triggeredAt + 72h) |
| `accessedAt` | LocalDateTime | O (접근 전 null) |

---

### 4.2 `GET /api/handover-events/{id}` — 이벤트 상세
**Response 200**: `ApiResponse<HandoverEventResponse>`
**에러**: 404, 403

### 4.3 `POST /api/handover-events/{id}/cancel` — 이벤트 취소
**Request Body**: 없음
**Response 200**: `ApiResponse<HandoverEventResponse>` (`message: "인계 이벤트 #{id}이(가) 취소되었습니다."`)
**에러**:
- 404 `EVENT_NOT_FOUND`
- 403 `FORBIDDEN_RESOURCE`
- 409 `INVALID_STATE_TRANSITION`: PENDING/NOTIFIED 외 상태(ACCESSED/EXPIRED/CANCELLED)에서 시도 시. 메시지 예: `"현재 상태 ACCESSED에서 CANCELLED(으)로 전이할 수 없습니다."`

> **갭 분석 (HandoverEvent)**
> - ✅ `트리거된 규칙 ID` — `ruleId`.
> - ⚠️ `상태 (대기중/전달완료/취소됨)` — 백엔드 enum 5개(PENDING/NOTIFIED/ACCESSED/EXPIRED/CANCELLED). 와이어프레임 매핑 권장: 대기중=PENDING+NOTIFIED, 전달완료=ACCESSED, 취소됨=CANCELLED. EXPIRED 별도 표시 추가 권장.
> - ✅ `발생 시각` — `triggeredAt`.
> - ⚠️ `유예기간 종료 시각 (cancel 가능 시간)` — `expiresAt`이 있긴 한데 의미가 다름: 이건 **토큰 만료** 시각 (트리거+72h). cancel은 사실상 **상태가 PENDING/NOTIFIED인 동안엔 항상 가능**(시간 제한 X). 즉, "cancel 가능 시간"은 시간이 아니라 **상태로 결정됨**. 와이어프레임이 시간 카운트다운을 표시하고 싶다면 백엔드에 별도 `gracePeriodEndsAt` 필드 신설 + 자동 NOTIFIED 전이 로직 필요.
> - ⚠️ `대상 자산 / 수신자 목록` — Event는 항상 단일 (assetId, recipientId) 페어임. **N×M 이벤트가 cross-product로 생성**됨. 와이어프레임이 "한 트리거의 모든 대상"을 한 번에 보여주려면 프론트가 `GET /api/handover-events?ruleId=...` 필터 호출하거나, 백엔드에 `GET /api/handover-rules/{id}/events` 신설 권장. 현재 백엔드엔 ruleId 필터 X.
> - ❌ `발동 사유 텍스트` 없음 — 어떤 조건(예: "30일 미접속")으로 발동됐는지 표시할 텍스트 없음. 추론 가능(`ruleId` → 규칙의 conditionType/value 조회)하지만 즉시 사용 가능한 필드는 없음. `HandoverEvent.triggerReason` 신설 권장.

---

## 5. HandoverAccess (수령인 공개 엔드포인트)

### `GET /api/handover-access/{token}` — 수령인 자산 조회 (1회용)

**인증**: **없음** (공개). 토큰 자체가 권한.

**Path Params**
| 이름 | 타입 | 비고 |
|---|---|---|
| `token` | String | base64url 인코딩 (≈43자) |

**Response 200**: `ApiResponse<HandoverAccessResponse>`
- `message`: `"자산이 조회되었습니다. 이 링크는 더 이상 사용할 수 없습니다."`

`HandoverAccessResponse`:
| 필드 | 타입 | nullable |
|---|---|---|
| `asset.title` | String | X |
| `asset.type` | `AssetType` | X |
| `asset.description` | String | O |
| `asset.content` | String | O (복호화된 평문) |
| `asset.externalRef` | String | O |
| `asset.sensitivityLevel` | `SensitivityLevel` | X |
| `rule.title` | String | X |
| `rule.description` | String | O |
| `owner.email` | String | X |
| `accessedAt` | LocalDateTime | X (요청 직후 채워짐) |
| `expiresAt` | LocalDateTime | X |
| `notice` | String | X (`"이 정보는 1회만 조회 가능합니다..."`) |

**에러**
| 상태 | code | 의미 |
|---|---|---|
| 404 | `INVALID_ACCESS_TOKEN` | 토큰 형식 OK지만 DB에 없음 |
| 410 | `ACCESS_TOKEN_EXPIRED` | `expiresAt` 경과 |
| 410 | `ACCESS_TOKEN_USED` | 이미 한 번 접근됨 (`accessedAt != null`) |
| 410 | `ACCESS_TOKEN_CANCELLED` | 소유자가 이벤트 취소함 |

**비즈니스 규칙**
- 호출 즉시 트랜잭션: 검증 통과 → `accessedAt = now`, `status = ACCESSED` → 자산 조회/반환
- **재호출 시 410 ACCESS_TOKEN_USED**. 다시 볼 수 없음.
- 토큰 만료 기본 **72시간** (`dahp.handover.access-token-validity = PT72H`)
- 자산 `content`는 `EncryptionService.decrypt()` 통과 후 반환 (NoOp이라 평문 그대로)

> **갭 분석 (HandoverAccess)**
> - ✅ `토큰 검증 결과` — 200/404/410 각 상황별 명확.
> - ✅ `자산 이름, 설명` — `asset.title`, `asset.description`.
> - ⚠️ `다운로드 URL` — `asset.externalRef`로 매핑 (외부 링크). **백엔드 자체 파일 호스팅 안 함** (S3 등 미구현, MVP). 다운로드 가능한 파일 자체는 없음, 사용자가 평소에 적어둔 외부 URL 클릭하는 방식.
> - ❌ `본인 인증 단계 정보 (이메일/SMS 코드)` 없음 — **수령인 인증 미구현**. 토큰만으로 접근. P2에서 OTP 추가 시 응답에 `requireOtp: true` + 별도 OTP 검증 엔드포인트 신설 필요.
> - ✅ `만료 시간` — `expiresAt`.

---

## 6. CheckIn (체크인)

### `POST /api/check-ins` — 수동 체크인
**Request Body**: **없음 (빈 body, Content-Type 없어도 됨)**
**Response 200**: `ApiResponse<CheckInStatusResponse>` (`message: "체크인 완료. 다음 만료일이 갱신되었습니다."`)

### `GET /api/check-ins/status` — 체크인 상태 조회
**Response 200**: `ApiResponse<CheckInStatusResponse>`
- `message` (overdue 여부에 따라 다름):
  - 정상: `"정상. 다음 체크인 만료까지 약 N일."`
  - overdue: `"체크인 만료 상태입니다. INACTIVITY_PERIOD 규칙이 다음 평가 시 자동 트리거될 수 있습니다."`

`CheckInStatusResponse`:
| 필드 | 타입 | nullable |
|---|---|---|
| `lastCheckInAt` | LocalDateTime | O (가입 직후엔 null) |
| `nextCheckInDueAt` | LocalDateTime | X (가입 시 createdAt + intervalDays로 자동 설정) |
| `checkInIntervalDays` | int | X |
| `overdue` | boolean | X |
| `daysUntilDue` | long | X (음수면 overdue 일수, 0 또는 양수면 남은 일수) |

**비즈니스 규칙**
- 체크인 시 `lastCheckInAt = now`, `nextCheckInDueAt = now + checkInIntervalDays`
- `checkInIntervalDays`는 가입 시 결정 (`SignupRequest`), `PATCH /api/users/me`로 변경 가능
- overdue 판단: `now > nextCheckInDueAt`

> **갭 분석 (CheckIn)** — 와이어프레임 요구 모두 충족 ✅

---

## 7. HandoverEvaluation (조건 평가 강제 실행, 시연용)

### `POST /api/handover/evaluation/evaluate-mine` — 내 ACTIVE 규칙 즉시 평가

**Request Body**: 없음
**Response 200**: `ApiResponse<EvaluationResult>`
- `message`: `"내 ACTIVE 규칙 N개 평가, M개 트리거됨."`

`EvaluationResult`:
| 필드 | 타입 |
|---|---|
| `evaluatedCount` | int |
| `triggeredCount` | int |
| `triggeredRuleIds` | `Long[]` |

**비즈니스 규칙**
- 본인의 ACTIVE 상태 규칙만 평가 (다른 사람 것 X)
- 조건 충족 시 자동으로 trigger되어 이벤트 N개 생성 + 알림 콘솔 출력
- 평가 로직 (`HandoverConditionEvaluator`):
  - MANUAL_APPROVAL: 평가 대상 아님 (always false)
  - SPECIFIC_DATE: `LocalDate.parse(conditionValue)` <= today
  - INACTIVITY_PERIOD: `lastCheckInAt(없으면 createdAt) + Integer.parseInt(conditionValue) days` < now
- conditionValue 파싱 실패 시 해당 규칙 평가 skip (warn 로그) — 트리거 안 됨, 에러 발생 안 함
- 스케줄러도 같은 로직 호출 (cron: `0 */1 * * * *` 기본, 1분마다)

> **갭 분석 (HandoverEvaluation)** — 와이어프레임에 별도 화면 없으면 미노출 가능. 시연/디버깅용 endpoint.

---

## 8. User 추가 필드 (마이페이지 와이어프레임용)

**현재 UserResponse 필드** (참고):
| 필드 | 타입 |
|---|---|
| `id` | Long |
| `email` | String |
| `role` | `UserRole` (USER / ADMIN) |
| `checkInIntervalDays` | int |
| `lastCheckInAt` | LocalDateTime (nullable) |
| `nextCheckInDueAt` | LocalDateTime |
| `createdAt` | LocalDateTime |

**현재 `PATCH /api/users/me`로 변경 가능한 것**: `checkInIntervalDays` 단 1개 (`UpdateUserRequest`).

> **갭 분석 (User 마이페이지)**
> - ❌ `이름`, `성` — User 엔티티에 없음. `firstName`, `lastName` 컬럼 신설 필요. 또는 `displayName` 한 필드로 간소화.
> - ❌ `전화번호` — User에 없음. `phone` 신설 필요.
> - ❌ `생년월일` — 없음. `birthDate` (LocalDate) 신설.
> - ❌ `알림 설정 (이메일/푸시/SMS/마케팅)` — 없음. `User.notificationPreferences` JSONB 또는 별도 `user_notification_settings` 테이블 신설.
> - ❌ `2단계 인증 활성화 여부` — 없음. `twoFactorEnabled` boolean + 2FA 흐름 전체 신설 (큰 작업, P2).
> - ❌ `언어 / 시간대 / 다크모드` — 없음. 클라이언트 측 localStorage 저장 권장 (서버 보낼 필요 X). 만약 서버 저장 원하면 `User.preferences` JSONB 추가.
>
> 추가 가능 여부: **전부 가능하지만 작업량 큼**. 마이페이지를 MVP에 포함하려면 어디까지 할지 우선순위 결정 필요. 추천 우선순위:
>   1. (필수) displayName 또는 name 1~2개 필드 — 인사말/UI 표시용
>   2. (선택) phone — 향후 SMS 알림 대비
>   3. (선택) preferences JSON — 다크모드/언어
>   4. (P2) 2FA, 다단계 알림 설정

---

## 9. 로그인 화면 (와이어프레임용)

> **갭 분석**
> - ❌ `비밀번호 찾기` 엔드포인트 — **미구현**. 추가하려면:
>   - `POST /api/auth/password-reset/request` (이메일 받아서 reset 토큰 발급, 이메일 발송 — `NotificationService` 확장 필요)
>   - `POST /api/auth/password-reset/confirm` (토큰 + 새 비밀번호)
>   - `PasswordResetToken` 엔티티 신설
>   - 최소 작업 1~2일.
> - ❌ `Google/GitHub OAuth` — **미구현, P2로 명시적 제외**됨. Spring Security OAuth2 Client + 각 provider 설정 필요. 시연용으론 비추.
> - ✅ 이메일/비밀번호 로그인은 `POST /api/auth/login` 정상 동작.

---

## 10. 핵심 누락 기능 요약 (프론트 설계 시 참고)

| 와이어프레임 요구 | 백엔드 상태 | 권장 대응 |
|---|---|---|
| Asset 카테고리 | ❌ | 백엔드 추가 (1시간) 또는 description prefix |
| Asset 사용자명 | ❌ | 백엔드 추가 (30분) 또는 content JSON |
| Asset 태그 (array) | ❌ | 백엔드 추가 (`@ElementCollection`, 2시간) |
| Asset content 실 암호화 | ⚠️ NoOp | EncryptionService 구현체 교체 (2~4시간) |
| Asset → 수신자 역참조 | ❌ | API 신설 (`/api/assets/{id}/recipients`, 1시간) |
| Recipient 관계 enum | ⚠️ String | 백엔드 enum 신설 (1시간) |
| Recipient 인증/접근권한 | ❌ | 큰 작업 (인증 흐름 전체, 1~2일+) |
| HandoverRule 유예기간 | ❌ | 컬럼 추가 + cancel/notification 흐름 변경 (반나절) |
| HandoverRule 사전 알림 다단계 | ❌ | 큰 작업 (스케줄링 + state machine, 1~2일) |
| HandoverRule 알림 채널 (SMS/푸시) | ❌ | 실 이메일/SMS 인프라 도입 필요 (P2) |
| HandoverEvent 트리거 사유 | ❌ | 컬럼 추가 (30분) |
| HandoverEvent ruleId 필터 | ❌ | Repository/Controller 메서드 추가 (30분) |
| HandoverAccess OTP | ❌ | 큰 작업 (1일) |
| User 이름/전화/생일 | ❌ | 컬럼 추가 (1시간) |
| User 알림/2FA/언어/다크모드 | ❌ | 부분 가능, 부분 P2 |
| 비밀번호 찾기 | ❌ | 신설 (1~2일) |
| OAuth (Google/GitHub) | ❌ | P2 |

---

## 부록: 백엔드 환경 변수 (참고)

| 변수 | 기본값 | 의미 |
|---|---|---|
| `DAHP_DB_URL` | `jdbc:postgresql://localhost:5433/dahp` | (application-local에 직접) |
| `DAHP_JWT_SECRET` | (로컬 더미) | JWT HS256 시크릿 |
| `DAHP_JWT_ACCESS_VALIDITY` | `PT1H` | access 토큰 1시간 |
| `DAHP_JWT_REFRESH_VALIDITY` | `P14D` | refresh 14일 |
| `DAHP_HANDOVER_TOKEN_VALIDITY` | `PT72H` | 수령인 토큰 72시간 |
| `DAHP_HANDOVER_EVAL_CRON` | `0 */1 * * * *` | 조건 평가 1분마다 |
| `DAHP_HANDOVER_EXPIRY_CRON` | `0 */5 * * * *` | 만료 정리 5분마다 |
| `DAHP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | CORS 허용 origin |
