# 프로젝트 패키지 구조

> 백엔드 코드의 패키지 분리 원칙과 초기 트리. Sprint 2 시작 시 이 구조에 맞춰 폴더 생성.

작성일: 2026-05-13

---

## 1. 아키텍처 스타일

**헥사고날 / 클린 아키텍처 스타일.** 도메인마다 5개 레이어 폴더로 분리:

| 폴더 | 책임 | 예시 |
|---|---|---|
| `controller` | REST 진입점, HTTP 직접 다룸. DTO도 여기에 포함 | `AuthController`, `dto/LoginRequest` |
| `application` | `@Service`, UseCase, 트랜잭션 경계, 비즈니스 흐름 조립 | `AuthService`, `JwtService` |
| `domain` | 도메인 모델(`@Entity`), Repository 인터페이스, 도메인 인터페이스(`OAuthClient`), 값객체, enum | `User`, `UserRepository`, `OAuthClient`(인터페이스) |
| `infrastructure` | Spring 기술 디테일, `domain` 인터페이스 구현체, 외부 시스템 어댑터 | `JwtAuthFilter`, `JwtTokenProvider`, `GoogleOAuthClient`(구현) |
| `exception` | 해당 도메인의 비즈니스 예외 클래스 | `AssetNotFoundException` |

### 의존성 방향
```
controller → application → domain
                ↑              ↑
            infrastructure ────┘
```

- `domain`은 어떤 외부 레이어도 의존하지 않음 (순수)
- `application`은 `domain`만 의존
- `controller`는 `application` 호출
- `infrastructure`는 `domain` 인터페이스를 구현하고, Spring 기술 디테일을 격리

---

## 2. 전체 트리

```
com.dahp
├── DahpApplication.java
├── global
│   ├── config
│   │   ├── SecurityConfig.java
│   │   ├── JpaAuditingConfig.java
│   │   └── OpenApiConfig.java
│   ├── security
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   ├── response
│   │   ├── ApiResponse.java
│   │   └── PageResponse.java
│   ├── common
│   │   └── BaseEntity.java
│   ├── exception
│   │   ├── BusinessException.java         ← 부모 예외
│   │   ├── ErrorCode.java                 ← enum
│   │   └── GlobalExceptionHandler.java    ← @RestControllerAdvice
│   └── util
│       └── TokenGenerator.java
└── domain
    ├── user
    │   ├── application
    │   │   └── UserService.java
    │   ├── controller
    │   │   ├── dto/
    │   │   │   ├── UpdateUserRequest.java
    │   │   │   └── UserResponse.java
    │   │   └── UserController.java
    │   ├── domain
    │   │   ├── User.java                  (@Entity)
    │   │   ├── UserRepository.java        (extends JpaRepository)
    │   │   └── UserRole.java              (enum)
    │   ├── exception
    │   │   └── UserNotFoundException.java
    │   └── infrastructure
    │       └── (필요 시)
    ├── auth
    │   ├── application
    │   │   ├── AuthService.java
    │   │   └── JwtService.java
    │   ├── controller
    │   │   ├── dto/
    │   │   │   ├── SignupRequest.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── LoginResponse.java
    │   │   │   └── TokenRefreshRequest.java
    │   │   └── AuthController.java
    │   ├── domain
    │   │   └── JwtToken.java              (값객체)
    │   ├── exception
    │   │   └── AuthenticationException.java
    │   └── infrastructure
    │       ├── JwtAuthFilter.java
    │       └── JwtTokenProvider.java
    ├── asset
    │   ├── application
    │   │   └── AssetService.java
    │   ├── controller
    │   │   ├── dto/
    │   │   │   ├── AssetCreateRequest.java
    │   │   │   ├── AssetUpdateRequest.java
    │   │   │   └── AssetResponse.java
    │   │   └── AssetController.java
    │   ├── domain
    │   │   ├── DigitalAsset.java          (@Entity)
    │   │   ├── AssetRepository.java       (extends JpaRepository)
    │   │   ├── EncryptionService.java     (인터페이스)
    │   │   ├── AssetType.java             (enum)
    │   │   └── SensitivityLevel.java      (enum)
    │   ├── exception
    │   │   └── AssetNotFoundException.java
    │   └── infrastructure
    │       └── NoOpEncryptionService.java  (EncryptionService 구현체)
    ├── recipient
    │   ├── application
    │   │   └── RecipientService.java
    │   ├── controller
    │   │   ├── dto/
    │   │   │   ├── RecipientCreateRequest.java
    │   │   │   ├── RecipientUpdateRequest.java
    │   │   │   └── RecipientResponse.java
    │   │   └── RecipientController.java
    │   ├── domain
    │   │   ├── Recipient.java
    │   │   └── RecipientRepository.java
    │   └── exception
    │       └── RecipientNotFoundException.java
    ├── handover
    │   ├── application
    │   │   ├── HandoverRuleService.java
    │   │   └── HandoverEventService.java
    │   ├── controller
    │   │   ├── dto/
    │   │   │   ├── HandoverRuleCreateRequest.java
    │   │   │   ├── HandoverRuleUpdateRequest.java
    │   │   │   ├── HandoverRuleResponse.java
    │   │   │   ├── HandoverEventResponse.java
    │   │   │   └── HandoverAccessResponse.java
    │   │   ├── HandoverRuleController.java
    │   │   ├── HandoverEventController.java
    │   │   └── HandoverAccessController.java  ← 공개 엔드포인트
    │   ├── domain
    │   │   ├── HandoverRule.java
    │   │   ├── HandoverRuleAsset.java         (M:N 매핑)
    │   │   ├── HandoverRuleRecipient.java     (M:N 매핑)
    │   │   ├── HandoverEvent.java
    │   │   ├── HandoverRuleRepository.java
    │   │   ├── HandoverRuleAssetRepository.java
    │   │   ├── HandoverRuleRecipientRepository.java
    │   │   ├── HandoverEventRepository.java
    │   │   ├── HandoverConditionType.java     (enum)
    │   │   ├── HandoverRuleStatus.java        (enum)
    │   │   └── HandoverEventStatus.java       (enum)
    │   └── exception
    │       ├── HandoverRuleNotFoundException.java
    │       ├── HandoverEventNotFoundException.java
    │       ├── InvalidStateTransitionException.java
    │       └── AccessTokenInvalidException.java
    ├── checkin
    │   ├── application
    │   │   └── CheckInService.java
    │   ├── controller
    │   │   ├── dto/
    │   │   │   └── CheckInStatusResponse.java
    │   │   └── CheckInController.java
    │   └── (별도 엔티티 없음 — User 엔티티에 필드 추가)
    ├── notification
    │   ├── application
    │   │   └── (필요 시 NotificationFacade)
    │   ├── domain
    │   │   ├── NotificationService.java       (인터페이스)
    │   │   ├── NotificationType.java          (enum)
    │   │   └── NotificationStatus.java        (enum)
    │   └── infrastructure
    │       └── ConsoleNotificationService.java  (MVP 구현체)
    └── audit
        ├── application
        │   └── AuditLogService.java
        ├── domain
        │   ├── AuditLog.java
        │   ├── AuditLogRepository.java
        │   └── AuditActionType.java
        └── controller
            ├── dto/
            │   └── AuditLogResponse.java
            └── AuditLogController.java        (ADMIN 전용)
```

---

## 3. 레이어별 상세 가이드

### 3.1 controller
- `@RestController`, `@RequestMapping("/api/...")`
- 메서드 시그니처: DTO in → DTO out
- 인증된 사용자는 `@AuthenticationPrincipal CustomUserDetails`로 받기
- HTTP 상태 코드는 `ResponseEntity` 또는 `@ResponseStatus`로 명시
- **비즈니스 로직 금지** — 무조건 `application` 위임

#### dto/
- Request DTO: 입력 검증(`@NotBlank`, `@Email`, `@Size` 등)
- Response DTO: `static from(entity)` 팩토리 메서드
- 페이징 응답은 `PageResponse<XxxResponse>` 래핑

### 3.2 application
- `@Service`, `@Transactional`
- UseCase = 비즈니스 메서드 (예: `signup()`, `triggerRule()`)
- 트랜잭션 경계는 여기. controller에 `@Transactional` 금지
- `domain.Repository` 호출, DTO ↔ Entity 변환
- 도메인 간 협력 시에도 여기서 조립

### 3.3 domain
- `@Entity` 클래스 + Lombok 어노테이션
- Repository는 **Spring Data JPA 인터페이스** (`extends JpaRepository<T, Long>`)
- 도메인 메서드는 엔티티에 직접 (예: `User.checkIn()`이 lastCheckInAt 갱신)
- 인터페이스 (외부 의존을 추상화한 것, 예: `OAuthClient`, `EncryptionService`)
- enum, 값객체
- **Spring import 금지** (원칙적으로). Lombok과 JPA 어노테이션은 허용

### 3.4 infrastructure
- Spring 기술 디테일: Filter, Interceptor, Provider, RestTemplate/WebClient 클라이언트
- `domain`의 인터페이스 구현체 (예: `NoOpEncryptionService implements EncryptionService`)
- 외부 시스템 어댑터 (S3, SMTP, OAuth 등)
- 커스텀 Repository 구현체 (QueryDsl 등)

### 3.5 exception
- 비즈니스 예외 클래스만 (`RuntimeException` 상속, `BusinessException` 자식)
- 각 예외에 `ErrorCode` 연결
- ErrorCode enum 자체와 `GlobalExceptionHandler`는 `com.dahp.global.exception`에 둠

---

## 4. global 패키지

도메인이 아닌 횡단 관심사:

| 폴더 | 내용 |
|---|---|
| `config` | `SecurityConfig`, `JpaAuditingConfig`, `OpenApiConfig` |
| `security` | `CustomUserDetails`, `CustomUserDetailsService` (Spring Security 통합) |
| `response` | `ApiResponse<T>`, `PageResponse<T>` |
| `common` | `BaseEntity` (`createdAt`, `updatedAt` `@MappedSuperclass`) |
| `exception` | `BusinessException` (부모), `ErrorCode` (enum), `GlobalExceptionHandler` |
| `util` | `TokenGenerator` (secure random) 등 헬퍼 |

**주의**: JWT 관련(`JwtTokenProvider`, `JwtAuthFilter`)은 `domain.auth.infrastructure`로 들어감. JWT는 인증 도메인의 기술 디테일이지 글로벌 인프라가 아님.

---

## 5. 도메인 의존성 규칙

- `auth` → `user` (사용자 조회)
- `asset` → `user` (소유자 검증)
- `recipient` → `user`
- `handover` → `user`, `asset`, `recipient`, `notification`
- `checkin` → `user`
- `audit` → 모든 도메인 (감사 대상)
- `notification` → 독립 (인터페이스만 노출)

**순환 의존 금지**. 발생 시 공통 인터페이스를 `global`로 추출.

---

## 6. 테스트 패키지

`src/test/java/com/dahp/` 아래 동일 미러링:

```
src/test/java/com/dahp
├── domain
│   ├── auth/application/AuthServiceTest.java
│   ├── asset/application/AssetServiceTest.java
│   └── handover/application/HandoverEventServiceTest.java
└── DahpApplicationTests.java
```

---

## 7. 참고: auth 도메인 예시 (실제 구조 샘플)

```
auth/
├── application/
│   ├── AuthService.java
│   ├── JwtService.java
│   ├── GoogleOAuthService.java         (P2: 소셜 로그인 추가 시)
│   ├── KakaoOAuthService.java          (P2)
│   ├── NaverOAuthService.java          (P2)
│   └── TokenBlacklistService.java      (P2: 로그아웃 토큰 무효화 시)
├── controller/
│   ├── dto/
│   └── AuthController.java
├── domain/
│   ├── JwtToken.java                   (값객체: access + refresh)
│   ├── OAuthClient.java                (P2: 소셜 로그인용 인터페이스)
│   └── OAuthUserInfo.java              (P2: 값객체)
├── exception/
│   └── AuthenticationException.java
└── infrastructure/
    ├── JwtAuthFilter.java              (Spring Security 필터)
    ├── JwtTokenProvider.java           (토큰 생성/파싱)
    ├── GoogleOAuthClient.java          (P2: OAuthClient 구현)
    ├── KakaoOAuthClient.java           (P2)
    └── NaverOAuthClient.java           (P2)
```

MVP에서는 OAuth 관련 5개 파일은 빠지고 일반 JWT 인증만 구현.

---

## 8. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 (기존 5폴더 평면 구조) |
| 2026-05-13 | 헥사고날 5폴더 구조로 전면 개편 (application/controller/domain/exception/infrastructure) |
