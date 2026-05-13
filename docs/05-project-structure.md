# 프로젝트 패키지 구조

> 백엔드 코드의 패키지 분리 원칙과 초기 트리. Sprint 2 시작 시 이 구조에 맞춰 폴더 생성.

작성일: 2026-05-13

---

## 1. 원칙

1. **`global`** (공통 인프라) + **`domain`** (도메인별 분리) 두 축
2. 도메인 폴더 안에 `controller`, `service`, `repository`, `entity`, `dto`, `enums`
3. **처음부터 잘게 쪼개지 않음** — 한 폴더에 5~7개 파일 들어가기 전까진 그대로
4. 도메인 간 직접 import는 허용하되, 가능한 한 서비스 레이어를 통해 호출

---

## 2. 트리

```
com.dahp
├── DahpApplication.java                ← 진입점
├── global
│   ├── config
│   │   ├── SecurityConfig.java         ← Spring Security 설정 + JWT 필터 등록
│   │   ├── JpaAuditingConfig.java      ← @EnableJpaAuditing (createdAt/updatedAt 자동)
│   │   └── OpenApiConfig.java          ← Swagger 메타 (제목, 보안 스키마)
│   ├── security
│   │   ├── JwtProvider.java            ← 토큰 발급 / 검증
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   ├── exception
│   │   ├── BusinessException.java      ← 비즈니스 예외 (ErrorCode 보유)
│   │   ├── ErrorCode.java              ← enum: 에러 코드 + 메시지 + HTTP 상태
│   │   └── GlobalExceptionHandler.java ← @RestControllerAdvice
│   ├── response
│   │   ├── ApiResponse.java            ← 공통 응답 래퍼
│   │   └── PageResponse.java           ← 페이징 응답 래퍼
│   ├── common
│   │   └── BaseEntity.java             ← createdAt, updatedAt (@MappedSuperclass)
│   └── util
│       └── TokenGenerator.java         ← secure random 토큰 생성
└── domain
    ├── user
    │   ├── controller/UserController.java
    │   ├── service/UserService.java
    │   ├── repository/UserRepository.java
    │   ├── entity/User.java
    │   ├── dto/
    │   │   ├── SignupRequest.java
    │   │   ├── UserResponse.java
    │   │   └── UpdateUserRequest.java
    │   └── enums/UserRole.java
    ├── auth
    │   ├── controller/AuthController.java
    │   ├── service/AuthService.java
    │   └── dto/
    │       ├── LoginRequest.java
    │       ├── LoginResponse.java        ← access + refresh 토큰
    │       └── TokenRefreshRequest.java
    ├── asset
    │   ├── controller/AssetController.java
    │   ├── service/
    │   │   ├── AssetService.java
    │   │   ├── EncryptionService.java    ← 인터페이스
    │   │   └── NoOpEncryptionService.java ← MVP 구현체
    │   ├── repository/AssetRepository.java
    │   ├── entity/DigitalAsset.java
    │   ├── dto/
    │   │   ├── AssetCreateRequest.java
    │   │   ├── AssetUpdateRequest.java
    │   │   └── AssetResponse.java
    │   └── enums/
    │       ├── AssetType.java
    │       └── SensitivityLevel.java
    ├── recipient
    │   ├── controller/RecipientController.java
    │   ├── service/RecipientService.java
    │   ├── repository/RecipientRepository.java
    │   ├── entity/Recipient.java
    │   └── dto/
    │       ├── RecipientCreateRequest.java
    │       ├── RecipientUpdateRequest.java
    │       └── RecipientResponse.java
    ├── handover
    │   ├── controller/
    │   │   ├── HandoverRuleController.java
    │   │   ├── HandoverEventController.java
    │   │   └── HandoverAccessController.java  ← 공개 엔드포인트
    │   ├── service/
    │   │   ├── HandoverRuleService.java
    │   │   └── HandoverEventService.java
    │   ├── repository/
    │   │   ├── HandoverRuleRepository.java
    │   │   ├── HandoverRuleAssetRepository.java
    │   │   ├── HandoverRuleRecipientRepository.java
    │   │   └── HandoverEventRepository.java
    │   ├── entity/
    │   │   ├── HandoverRule.java
    │   │   ├── HandoverRuleAsset.java       ← M:N 매핑
    │   │   ├── HandoverRuleRecipient.java   ← M:N 매핑
    │   │   └── HandoverEvent.java
    │   ├── dto/
    │   │   ├── HandoverRuleCreateRequest.java
    │   │   ├── HandoverRuleUpdateRequest.java
    │   │   ├── HandoverRuleResponse.java
    │   │   ├── HandoverEventResponse.java
    │   │   └── HandoverAccessResponse.java  ← 토큰 조회 시 자산 내용 반환
    │   └── enums/
    │       ├── HandoverConditionType.java
    │       ├── HandoverRuleStatus.java
    │       └── HandoverEventStatus.java
    ├── checkin
    │   ├── controller/CheckInController.java
    │   ├── service/CheckInService.java
    │   └── dto/CheckInStatusResponse.java
    │   ← Note: 별도 엔티티 없음. User 엔티티에 필드 추가.
    ├── notification
    │   ├── service/
    │   │   ├── NotificationService.java       ← 인터페이스
    │   │   └── ConsoleNotificationService.java ← MVP 구현체
    │   └── enums/
    │       ├── NotificationType.java
    │       └── NotificationStatus.java
    │   ← Note: Notification 엔티티는 P2로 연기. MVP는 콘솔 출력만.
    └── audit
        ├── service/AuditLogService.java
        ├── repository/AuditLogRepository.java
        ├── entity/AuditLog.java
        └── enums/AuditActionType.java
        ← Note: Sprint 4에서 추가
```

---

## 3. 도메인별 책임 요약

| 패키지 | 책임 |
|---|---|
| `user` | 사용자 프로필 관리 (가입, 조회, 수정) |
| `auth` | 인증 흐름 (로그인, 토큰 발급, 갱신) |
| `asset` | 디지털 자산 CRUD + 암호화 인터페이스 |
| `recipient` | 수령인 관리 |
| `handover` | 규칙 정의, 트리거, 이벤트 생성, 토큰 접근 |
| `checkin` | 사용자 체크인 (User 엔티티 확장) |
| `notification` | 알림 발송 (MVP는 콘솔) |
| `audit` | 감사 로그 (Sprint 4) |

---

## 4. 도메인 의존성 규칙

- `auth` → `user` (사용자 조회 필요)
- `asset` → `user` (소유자 검증)
- `recipient` → `user` (소유자 검증)
- `handover` → `user`, `asset`, `recipient`, `notification` (인계 발생 시 알림)
- `checkin` → `user`
- `audit` → 모든 도메인 (감사 대상)
- `notification` → 독립 (인터페이스만 제공)

**순환 의존 금지.** 만약 발생하면 공통 인터페이스를 `global` 아래로 추출.

---

## 5. 테스트 패키지

`src/test/java/com/dahp/` 아래 동일 구조로 미러링:

```
src/test/java/com/dahp
├── domain
│   ├── auth/AuthServiceTest.java
│   ├── asset/AssetServiceTest.java
│   └── handover/HandoverEventServiceTest.java
└── DahpApplicationTests.java
```

각 서비스 단위 테스트 + 핵심 컨트롤러 `@WebMvcTest` 1~2개 (Sprint 4).

---

## 6. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 |
