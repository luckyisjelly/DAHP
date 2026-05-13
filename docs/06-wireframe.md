# 화면 흐름도 (Wireframe / Screen Flow)

> DAHP는 백엔드 프로젝트이지만, 클라이언트가 어떻게 API를 호출할지 흐름이 명확해야 API 설계가 정합성을 가짐. 이 문서는 가상 클라이언트의 화면 전이 흐름을 정리.

작성일: 2026-05-13

---

## 1. 사용자 (Primary User) 화면 흐름

### 1.1 전체 흐름도

```mermaid
flowchart TD
    Start([앱 진입]) --> HasToken{토큰 있음?}
    HasToken -- No --> Auth[인증 화면]
    HasToken -- Yes --> Home[홈/대시보드]

    Auth --> Signup[회원가입]
    Auth --> Login[로그인]
    Signup -- POST /auth/signup --> AutoLogin[자동 로그인]
    Login -- POST /auth/login --> Home
    AutoLogin -- POST /auth/login --> Home

    Home --> AssetList[자산 목록]
    Home --> RecipientList[수령인 목록]
    Home --> RuleList[규칙 목록]
    Home --> EventList[이벤트 이력]
    Home --> CheckIn[체크인 상태]
    Home --> Profile[내 정보]

    AssetList -- POST /assets --> AssetForm[자산 생성/수정 폼]
    AssetList -- GET /assets/id --> AssetDetail[자산 상세]
    AssetForm -- 저장 --> AssetList

    RecipientList -- POST /recipients --> RecipientForm[수령인 생성/수정 폼]
    RecipientList -- GET /recipients/id --> RecipientDetail[수령인 상세]

    RuleList -- POST /rules --> RuleForm[규칙 생성/수정 폼<br/>자산, 수령인 선택]
    RuleList -- GET /rules/id --> RuleDetail[규칙 상세]
    RuleDetail -- activate/pause --> RuleDetail
    RuleDetail -- trigger 버튼 --> TriggerConfirm{확인 다이얼로그}
    TriggerConfirm -- 확인 --> TriggerResult[트리거 결과<br/>이벤트 N개 생성됨]

    EventList -- GET /events/id --> EventDetail[이벤트 상세]
    EventDetail -- cancel --> EventDetail

    CheckIn -- POST /check-ins --> CheckIn

    Profile -- PATCH /users/me --> Profile
    Profile -- 로그아웃 --> Auth
```

---

### 1.2 화면별 호출 API

| 화면 | 호출 API | 비고 |
|---|---|---|
| 회원가입 | `POST /api/auth/signup` → `POST /api/auth/login` | 가입 후 자동 로그인 |
| 로그인 | `POST /api/auth/login` | 토큰 저장 |
| 홈/대시보드 | `GET /api/users/me`, `GET /api/check-ins/status` | 환영 메시지 + 체크인 상태 위젯 |
| 자산 목록 | `GET /api/assets?page=0&size=20&type=&q=` | 필터/검색 |
| 자산 상세 | `GET /api/assets/{id}` | |
| 자산 생성 폼 | `POST /api/assets` | |
| 자산 수정 폼 | `GET /api/assets/{id}` → `PATCH /api/assets/{id}` | |
| 자산 삭제 | `DELETE /api/assets/{id}` | 확인 다이얼로그 |
| 수령인 목록 | `GET /api/recipients` | |
| 수령인 폼 | `POST` / `PATCH /api/recipients` | |
| 규칙 목록 | `GET /api/handover-rules` | 상태별 필터 가능 |
| 규칙 생성 폼 | `GET /api/assets` + `GET /api/recipients` → `POST /api/handover-rules` | 자산/수령인 다중 선택 |
| 규칙 상세 | `GET /api/handover-rules/{id}` | 포함된 자산/수령인 목록 |
| 규칙 활성화 | `POST /api/handover-rules/{id}/activate` | |
| 규칙 일시정지 | `POST /api/handover-rules/{id}/pause` | |
| **규칙 트리거** | `POST /api/handover-rules/{id}/trigger` | 확인 다이얼로그 필수 |
| 트리거 결과 | (응답 화면) | 발생한 이벤트 목록 표시 |
| 이벤트 이력 | `GET /api/handover-events` | |
| 이벤트 상세 | `GET /api/handover-events/{id}` | 토큰 원본은 표시 X |
| 이벤트 취소 | `POST /api/handover-events/{id}/cancel` | |
| 체크인 | `POST /api/check-ins` | 버튼 하나 |
| 내 정보 | `GET /api/users/me`, `PATCH /api/users/me` | 체크인 주기 변경 |

---

## 2. 수령인 (Recipient) 화면 흐름

수령인은 **회원가입 없음**. 이메일로 받은 링크를 클릭해 단일 화면 접근.

```mermaid
flowchart TD
    Email[알림 이메일/콘솔<br/>토큰 링크 포함] -- 링크 클릭 --> AccessPage[자산 조회 페이지]
    AccessPage -- GET /handover-access/token --> Decision{토큰 검증}
    Decision -- OK --> AssetView[자산 내용 표시<br/>1회 조회 안내]
    Decision -- 만료 --> ExpiredPage[만료 안내]
    Decision -- 사용됨 --> UsedPage[이미 사용됨 안내]
    Decision -- 잘못됨 --> NotFoundPage[유효하지 않은 링크]
    AssetView -- 닫기 --> End([종료])
```

### 2.1 접근 페이지 응답 처리

| HTTP 응답 | 화면 |
|---|---|
| 200 | 자산 내용 표시 ("이 정보는 1회만 조회 가능합니다" 안내) |
| 404 INVALID_TOKEN | "유효하지 않은 링크입니다" |
| 410 ACCESS_TOKEN_EXPIRED | "이 링크는 만료되었습니다. 소유자에게 문의하세요." |
| 410 ACCESS_TOKEN_USED | "이 링크는 이미 사용되었습니다." |

---

## 3. Admin 화면 흐름 (선택, Sprint 4)

```mermaid
flowchart TD
    AdminLogin[ADMIN 로그인] -- POST /auth/login --> AdminHome[관리자 대시보드]
    AdminHome --> AuditList[감사 로그 조회]
    AuditList -- GET /admin/audit-logs --> AuditList
    AdminHome --> UserSearch[사용자 검색<br/>P2]
```

---

## 4. 트리거 시 알림 흐름 (백엔드 내부)

```mermaid
sequenceDiagram
    actor Owner as 소유자
    participant API as DAHP API
    participant DB as PostgreSQL
    participant Notify as NotificationService
    actor Recipient as 수령인

    Owner ->> API: POST /handover-rules/{id}/trigger
    API ->> DB: 규칙 조회 + 자산/수령인 로드
    API ->> DB: 각 (자산, 수령인) 조합으로 HandoverEvent 생성
    API ->> DB: accessToken 발급, 해시만 저장
    API ->> DB: 규칙 상태 = TRIGGERED
    API ->> Notify: 각 이벤트에 대해 notify(recipient, token, asset)
    Notify -->> Notify: 콘솔 출력 (MVP)
    Note over Notify, Recipient: P2: 실제 이메일/SMS 발송
    API -->> Owner: 200 OK, 이벤트 목록 반환
```

---

## 5. 수령인 토큰 접근 시퀀스

```mermaid
sequenceDiagram
    actor Recipient as 수령인
    participant API as DAHP API
    participant DB as PostgreSQL

    Recipient ->> API: GET /handover-access/{token}
    API ->> API: token을 SHA-256 해시
    API ->> DB: SELECT FROM handover_events WHERE access_token_hash = ?
    alt 토큰 없음
        DB -->> API: empty
        API -->> Recipient: 404 INVALID_TOKEN
    else 만료됨
        DB -->> API: 이벤트(expires_at < now)
        API -->> Recipient: 410 ACCESS_TOKEN_EXPIRED
    else 이미 사용됨
        DB -->> API: 이벤트(accessed_at != null)
        API -->> Recipient: 410 ACCESS_TOKEN_USED
    else 정상
        API ->> DB: UPDATE accessed_at = now, status = ACCESSED
        API ->> DB: AuditLog: HANDOVER_ACCESSED
        DB -->> API: ok
        API -->> Recipient: 200 OK, 자산 내용
    end
```

---

## 6. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 |
