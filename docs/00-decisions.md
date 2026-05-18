# DAHP 프로젝트 결정사항

> 이 문서는 Sprint 1에서 결정된 기술 스택과 설계 방향을 기록합니다. 변경 시 이 문서를 먼저 수정한 뒤 코드를 따라갑니다.

작성일: 2026-05-13

---

## 1. 기술 스택

| 항목 | 선택 | 비고 |
|---|---|---|
| 언어 | **Java 21** | LTS, 가상 스레드 활용 여지 |
| 프레임워크 | **Spring Boot 4.0.6** | 최신 안정 버전 |
| 빌드 도구 | **Gradle** | `build.gradle` 그루비 DSL |
| 데이터베이스 | **PostgreSQL** | JSONB 컬럼 활용 여지, 표준 준수 |
| ORM | **Spring Data JPA** | 단순 CRUD는 메서드 쿼리, 복잡 쿼리는 `@Query` |
| 인증 | **JWT (jjwt 라이브러리)** | access + refresh 토큰 분리 |
| 비밀번호 해시 | **BCrypt** | Spring Security 기본 |
| 검증 | **Bean Validation (Jakarta Validation)** | DTO에 `@Valid` 적용 |
| API 문서 | **springdoc-openapi (Swagger UI)** | `/swagger-ui.html` |
| 보일러플레이트 감소 | **Lombok** | `@Getter`, `@Builder`, `@RequiredArgsConstructor` 위주 |
| 테스트 | **JUnit 5 + Mockito + Spring Boot Test** | |
| 테스트 DB | **H2 in-memory** | `application-test.yml`에서 분리 |

---

## 2. 일정

총 4 sprint × 약 1주 = **약 1개월**

| Sprint | 기간 | 목표 |
|---|---|---|
| Sprint 1 | 4일 | 요구사항 분석 + 설계 (문서) |
| Sprint 2 | 7일 | 기반 + Auth + Asset CRUD |
| Sprint 3 | 9~10일 | 인계 플로우 풀스택 (가장 무거움) |
| Sprint 4 | 7일 | 테스트 + AuditLog + 문서 + 시연 준비 |

**Sprint 3을 다른 sprint보다 길게** 잡은 이유: 9개 sprint로 분해 시 4개 sprint(Recipient, Rule, Event, Check-in 일부, Notification 일부)에 해당하는 작업량을 한 sprint에 집어넣었기 때문.

---

## 3. 아키텍처 방향

### 3.1 레이어 구조
**헥사고날 / 클린 아키텍처 스타일** 채택. 도메인마다 5개 레이어로 분리:

```
controller (HTTP) → application (UseCase, @Service) → domain (Entity, Repository 인터페이스)
                                       ↑
                            infrastructure (구현체, Spring 기술 디테일)
                                       ↓
                                  domain 인터페이스
```

| 레이어 | 책임 |
|---|---|
| `controller` | REST 진입점, DTO 입출력 (DTO는 `controller/dto/`) |
| `application` | `@Service`, UseCase, 트랜잭션 경계 |
| `domain` | `@Entity`, Repository 인터페이스, 도메인 인터페이스, enum, 값객체 — 순수 |
| `infrastructure` | Spring 기술 디테일(Filter, Provider), 외부 어댑터, domain 인터페이스 구현체 |
| `exception` | 도메인별 비즈니스 예외 (`ErrorCode`/Handler는 `global`) |

자세한 트리 및 가이드는 [05-project-structure.md](05-project-structure.md) 참고.

### 3.2 패키지 구조 원칙
- `global` (공통 인프라) + `domain` (도메인별 분리) 두 축
- 도메인 하위에 5개 레이어 폴더: `application`, `controller`, `domain`, `exception`, `infrastructure`
- **DTO는 `controller/dto/`에 둠** (web concern 으로 본다)
- **Repository 인터페이스는 `domain/`** (Spring Data JPA `extends JpaRepository`)
- **인터페이스의 구현체는 `infrastructure/`** (예: `EncryptionService` → `NoOpEncryptionService`)
- **JWT 관련은 `domain.auth.infrastructure/`** (인증 도메인의 기술 디테일)

자세한 트리는 [05-project-structure.md](05-project-structure.md) 참고.

### 3.3 DTO 정책
- 엔티티를 컨트롤러로 노출하지 않음
- 요청용 `XxxRequest`, 응답용 `XxxResponse` 분리
- 변환은 서비스 계층 또는 정적 팩토리 메서드(`XxxResponse.from(entity)`)에서

### 3.4 공통 응답 포맷
모든 API는 `ApiResponse<T>`로 래핑:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-05-13T09:37:00+09:00"
}
```

에러:
```json
{
  "success": false,
  "data": null,
  "error": { "code": "ASSET_NOT_FOUND", "message": "자산을 찾을 수 없습니다." }
}
```

### 3.5 ID 정책
- 내부 PK: **`Long` (BIGINT)** 자동 증가
- 외부 URL에 노출되는 식별자: 자산/규칙/이벤트는 그대로 `Long` 사용. 단 **수령인 접근 토큰만은 secure random** (32바이트 base64url)

---

## 4. 보안 정책

### 4.1 인증
- **JWT 기반**, 무상태
- access token: **1시간 만료**
- refresh token: **14일 만료**, DB 저장(MVP는 in-memory도 허용)
- 비밀번호: BCrypt (cost 10)
- 시크릿 키: 환경 변수에서 주입 (`application.yml`에 하드코딩 금지)

### 4.2 인가
- 모든 자산/수령인/규칙 API에 **소유자 검증** (`entity.ownerId == currentUser.id`)
- 서비스 계층에서 `assertOwnership()` 유틸로 일관 처리
- 수령인 접근 엔드포인트(`/api/handover-access/{token}`)만 공개 — 토큰 자체가 권한

### 4.3 토큰 보안 (수령인 접근용)
- 길이: 32바이트 random → base64url (≈43자)
- DB 저장: **해시만** (SHA-256). 원본은 발급 시 1회만 응답에 노출
- 만료: 기본 72시간
- 1회 사용: `accessedAt` 채워지면 재사용 불가

### 4.4 민감 데이터 처리
- `DigitalAsset.content` 컬럼은 처음부터 **암호화 가능 구조**로 설계
- MVP는 `NoOpEncryptionService` (평문 그대로 반환)
- 인터페이스(`EncryptionService`)만 정의해두면 후속에 AES-GCM 구현체로 교체 가능
- `contentEncrypted` boolean 컬럼 미리 추가

### 4.5 감사 로그
다음 5개 액션만 MVP에서 로깅 (Sprint 4):
- `USER_LOGIN`
- `ASSET_CREATED`, `ASSET_DELETED`
- `RULE_TRIGGERED`
- `HANDOVER_ACCESSED`

---

## 5. MVP 범위 결정

### 5.1 포함
- 사용자 인증 (가입, 로그인, JWT)
- 디지털 자산 CRUD
- 수령인 CRUD (이메일/이름/관계 등)
- 인계 규칙 CRUD
- **수동 trigger** (`POST /handover-rules/{id}/trigger`)
- **자동 조건 평가 스케줄러** (`@Scheduled`):
  - `MANUAL_APPROVAL`: 수동 trigger만
  - `SPECIFIC_DATE`: 지정 날짜 도래 시 자동 트리거
  - `INACTIVITY_PERIOD`: 사용자가 N일 이상 미체크인 시 자동 트리거
- HandoverEvent 생성 + accessToken 발급 (72시간 만료, 1회 사용)
- 수령인 토큰 기반 자산 조회 (인증 없음, 토큰만)
- 수동 체크인 (lastCheckInAt 갱신, 기본 주기 30일)
- 콘솔 모킹 알림
- 감사 로그 (Sprint 4)

### 5.2 제외 (P2 이후)
- **조건 타입 일부**: `PERIODIC_CHECK_FAILED`, `EMERGENCY_REQUEST`
- **수령인 OTP 추가 인증** — MVP는 토큰만으로 접근
- **소셜 로그인** (Google/Kakao/Naver OAuth) — Spring Security OAuth2 client
- **실제 이메일/SMS 발송** — `ConsoleNotificationService`로 모킹
- **실제 암호화 구현** — 인터페이스만 준비
- **파일 업로드** — 자산은 URL/텍스트 참조만 저장 (S3 통합 X)
- **수령인 회원가입** — 수령인은 별도 엔티티, User role 아님
- **다중 인스턴스 배포 / Redis** — 단일 인스턴스 가정

### 5.3 시연 전략 (자동 평가 관련)
스케줄러가 매시간 또는 매일 실행되므로 시연 중 INACTIVITY 30일을 기다릴 수 없음. 다음 전략 채택:
- **시연용 프로파일**: `application-demo.yml`에서 스케줄러 주기를 **1분**으로 단축
- 시연 직전에 INACTIVITY 규칙의 condition_value를 짧게(예: 1분) 설정
- 또는 manual trigger API로 직접 보여줌 (가장 단순)

---

## 6. 포지셔닝

서비스 설명에서 **"디지털 유언"·"사망 시 인계"** 같은 표현은 사용하지 않음.
대신 **"조건 기반 디지털 자산 인계 플랫폼"** 으로 통일.

이유:
- 사망/상속 시나리오는 법률·신원확인·증명 요건이 폭증
- 학기 프로젝트 범위 초과
- "임의 조건 인계"로 포지셔닝하면 기술적 흥미는 유지하면서 법적 함정 회피

---

## 7. 패키지 네이밍

`build.gradle`의 `group`이 `com.dahp`이고, 현재 코드의 root package도 `com.dahp`임.

**결정**: `com.dahp.backend`이 아닌 `com.dahp`을 root로 사용. ZIP에 이미 그렇게 되어 있고, 차이는 미미함.

---

## 8. 환경 / 프로파일

3개 프로파일 사용:
- `local`: 개발자 로컬 (PostgreSQL 직접 설치, devtools 활성)
- `test`: 테스트 (H2 in-memory)
- `prod`: 배포 (환경 변수로 DB 접속 정보 주입) — Sprint 4 시점 결정

`application.yml` 하나에 프로파일별 섹션을 두거나, `application-{profile}.yml` 분리.
권장: 분리 방식 (가독성).

---

## 9. 개발 프로세스

### 9.1 팀 규모
**솔로 (1인 작업).** PR 리뷰는 자기 리뷰. 학습 목적상 PR 워크플로우는 유지.

### 9.2 커밋 메시지 컨벤션
**Conventional Commits 영문 타입 + 한글 본문.**

타입:
- `feat:` 새 기능
- `fix:` 버그 수정
- `docs:` 문서
- `refactor:` 리팩터링 (기능 변화 없음)
- `test:` 테스트 추가/수정
- `chore:` 빌드/설정/잡일
- `style:` 포맷팅, 코드 스타일

형식:
```
<type>: <짧은 한글 요약>

<상세 본문 (한글, 옵션)>
```

예시:
```
feat: 자산 CRUD API 구현

POST/GET/PATCH/DELETE /api/assets 5개 엔드포인트 추가.
소유자 검증 적용.
```

**Co-Authored-By: Claude 트레일러 추가 X.** 본인 단독 작성자.

### 9.3 브랜치 전략
**feature 브랜치 + PR (Squash merge).**

- 기본 브랜치: `main`
- 작업 브랜치: `feat/{도메인}-{기능}`, `fix/{이슈}`, `docs/{주제}` 등
- 작업 완료 시 PR 생성 → 자기 리뷰 → **Squash merge** (1 PR = 1 main 커밋)
- main 직접 push 지양 (긴급 hotfix 제외)

예시:
- `feat/auth-jwt`
- `feat/asset-crud`
- `feat/handover-rule-trigger`
- `docs/sprint1-revisions`
- `fix/asset-ownership-validation`

### 9.4 코드 스타일
- IntelliJ 기본 Java 포맷팅 (탭 X, 스페이스 4)
- import 정렬: IntelliJ 기본
- 한 줄 길이: 120자 권장
- `final` 키워드 적극 사용 (불변성)
- Lombok 사용 (`@RequiredArgsConstructor`, `@Getter` 위주, `@Data` 지양)

---

## 10. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 (Sprint 1 결정) |
| 2026-05-13 | 아키텍처를 헥사고날/클린 5-레이어 구조로 변경 (application/controller/domain/exception/infrastructure) |
| 2026-05-13 | MVP 범위 조정: 자동 조건 평가 스케줄러 포함, 소셜 로그인 P2로 이동. 개발 프로세스 섹션 추가 (솔로, Conventional Commits, feature 브랜치 + PR) |
