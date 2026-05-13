# 요구사항 명세

> DAHP의 기능/비기능 요구사항 정리. MVP 범위 명시.

작성일: 2026-05-13

---

## 1. 프로젝트 개요

### 1.1 정의
**DAHP** (Digital Asset Handover Platform): 사용자가 디지털 자산을 등록하고, 신뢰하는 수령인과 인계 조건을 미리 설정해두면, 조건 충족 시 시스템이 안전한 접근 링크를 발급해 자산을 인계하는 백엔드 플랫폼.

### 1.2 포지셔닝
- ✅ **"조건 기반 디지털 자산 인계 플랫폼"**
- ❌ "디지털 유언장", "사망 시 인계"

(이유: 사망/상속은 법률 함정 다수. [00-decisions.md](00-decisions.md) §6 참고)

### 1.3 해결하려는 문제
| 문제 | 시나리오 |
|---|---|
| 디지털 자산이 여러 플랫폼에 흩어져 관리 어려움 | 비밀번호 매니저, 클라우드, SNS 등 분산 |
| 사용자가 접근 불가 상태일 때 가족/지인이 정보 못 찾음 | 비상 상황, 출장, 일시적 부재 등 |
| 미리 공유하기엔 보안 위험 | 비밀번호를 평소에 알려주는 것은 불안 |
| 기존 "디지털 유언장"은 사망에만 초점 | 일반적 비상 대비 옵션 부족 |

### 1.4 사용자 역할
| 역할 | 설명 |
|---|---|
| **Primary User** | 자산 등록·관리·인계 조건 설정 (가입 회원) |
| **Recipient** | 조건 충족 시 자산 접근 (별도 회원가입 X, 토큰 기반) |
| **Admin** | 시스템 관리, 감사 로그 조회 (MVP는 최소화) |

---

## 2. 기능 요구사항

### 2.1 인증 (Sprint 2)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-AUTH-01 | 이메일 + 비밀번호로 회원가입 | MVP |
| F-AUTH-02 | 비밀번호 BCrypt 해시 저장 | MVP |
| F-AUTH-03 | 로그인 시 JWT access + refresh 토큰 발급 | MVP |
| F-AUTH-04 | refresh 토큰으로 access 토큰 재발급 | MVP |
| F-AUTH-05 | 로그아웃 (refresh 토큰 무효화) | MVP |
| F-AUTH-06 | 사용자 역할(USER/ADMIN) 구분 | MVP |
| F-AUTH-07 | OAuth 소셜 로그인 (Google, Naver 등) | P2 |
| F-AUTH-08 | 비밀번호 재설정 (이메일 링크) | P2 |

### 2.2 사용자 정보 (Sprint 2)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-USER-01 | 내 정보 조회 (`/users/me`) | MVP |
| F-USER-02 | 체크인 주기 변경 | MVP |
| F-USER-03 | 회원 탈퇴 (cascade 삭제 또는 soft delete) | P2 |

### 2.3 디지털 자산 관리 (Sprint 2)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-ASSET-01 | 자산 생성 (제목, 타입, 내용, 외부참조, 민감도) | MVP |
| F-ASSET-02 | 자산 목록 조회 (페이징, 타입/검색어 필터) | MVP |
| F-ASSET-03 | 자산 상세 조회 | MVP |
| F-ASSET-04 | 자산 수정 (부분 업데이트) | MVP |
| F-ASSET-05 | 자산 삭제 | MVP |
| F-ASSET-06 | 소유자만 자기 자산 접근 가능 (인가) | MVP |
| F-ASSET-07 | 자산 타입: ACCOUNT, FILE, NOTE, LINK, MESSAGE, DOCUMENT, ETC | MVP |
| F-ASSET-08 | 민감도 레벨: LOW, MEDIUM, HIGH | MVP |
| F-ASSET-09 | content 컬럼 암호화 (실제) | P2 |
| F-ASSET-10 | 실제 파일 업로드 (S3 등) | P2 |

### 2.4 수령인 관리 (Sprint 3)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-RECIP-01 | 수령인 등록 (이름, 이메일, 전화, 관계, 메모) | MVP |
| F-RECIP-02 | 수령인 목록/상세/수정/삭제 | MVP |
| F-RECIP-03 | 소유자만 자기 수령인 접근 가능 (인가) | MVP |
| F-RECIP-04 | 수령인이 DAHP 회원이면 자동 매칭 | P2 |

### 2.5 인계 규칙 관리 (Sprint 3)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-RULE-01 | 인계 규칙 생성 (제목, 조건 타입, 자산 N개, 수령인 N명) | MVP |
| F-RULE-02 | 규칙 목록/상세/수정/삭제 | MVP |
| F-RULE-03 | 규칙 활성화/일시정지 (상태 전이) | MVP |
| F-RULE-04 | 조건 타입: MANUAL_APPROVAL, SPECIFIC_DATE 지원 (수동 평가) | MVP |
| F-RULE-05 | 규칙 생성 시 자산·수령인 소유자 일치 검증 | MVP |
| F-RULE-06 | 조건 타입: INACTIVITY_PERIOD, PERIODIC_CHECK_FAILED, EMERGENCY_REQUEST | P2 |
| F-RULE-07 | 자동 조건 평가 스케줄러 (`@Scheduled`) | P2 |

### 2.6 인계 트리거 & 이벤트 (Sprint 3)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-TRIG-01 | 수동 트리거 API (소유자 호출) | MVP |
| F-TRIG-02 | 트리거 시 자산 × 수령인 cross product로 이벤트 생성 | MVP |
| F-TRIG-03 | 각 이벤트마다 secure random accessToken 발급 (32바이트, base64url) | MVP |
| F-TRIG-04 | accessToken은 DB에 SHA-256 해시만 저장 | MVP |
| F-TRIG-05 | 이벤트 만료 시간 설정 (기본 72시간) | MVP |
| F-TRIG-06 | 이벤트 상태: PENDING, NOTIFIED, ACCESSED, COMPLETED, EXPIRED, CANCELLED | MVP |
| F-TRIG-07 | 이벤트 취소 API (소유자) | MVP |

### 2.7 수령인 접근 플로우 (Sprint 3)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-ACCESS-01 | 토큰 기반 자산 조회 (`/handover-access/{token}`, 인증 없음) | MVP |
| F-ACCESS-02 | 1회 사용 후 ACCESSED 상태 전이 | MVP |
| F-ACCESS-03 | 만료된 토큰 410 응답 | MVP |
| F-ACCESS-04 | 이미 사용된 토큰 410 응답 | MVP |
| F-ACCESS-05 | OTP/이메일 2차 인증 | P2 |

### 2.8 체크인 (Sprint 3 또는 4)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-CHECK-01 | 수동 체크인 API (lastCheckInAt 갱신) | MVP |
| F-CHECK-02 | 체크인 상태 조회 (다음 만료일, overdue 여부) | MVP |
| F-CHECK-03 | 체크인 만료 시 자동 트리거 | P2 |
| F-CHECK-04 | 체크인 알림 (이메일/SMS) | P2 |

### 2.9 알림 (Sprint 3)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-NOTIF-01 | `NotificationService` 인터페이스 정의 | MVP |
| F-NOTIF-02 | `ConsoleNotificationService` 구현 (System.out 출력) | MVP |
| F-NOTIF-03 | 트리거 시 수령인에게 알림 발송 (콘솔) | MVP |
| F-NOTIF-04 | 실제 이메일 발송 (SendGrid, SES 등) | P2 |
| F-NOTIF-05 | SMS 발송 | P2 |
| F-NOTIF-06 | Notification 엔티티 + 이력 조회 | P2 |

### 2.10 감사 로그 (Sprint 4)
| ID | 기능 | 우선순위 |
|---|---|---|
| F-AUDIT-01 | 주요 액션 5개 로깅 (USER_LOGIN, ASSET_CREATED, ASSET_DELETED, RULE_TRIGGERED, HANDOVER_ACCESSED) | MVP |
| F-AUDIT-02 | IP 주소 기록 | MVP |
| F-AUDIT-03 | ADMIN의 감사 로그 조회 API | MVP |
| F-AUDIT-04 | AOP 기반 자동 로깅 | P2 |

---

## 3. 비기능 요구사항

### 3.1 보안
| ID | 요구사항 | 우선순위 |
|---|---|---|
| NF-SEC-01 | 비밀번호는 BCrypt 해시 저장 | MVP |
| NF-SEC-02 | JWT 시크릿은 환경 변수에서 주입 | MVP |
| NF-SEC-03 | 모든 자원 API에 소유자 검증 | MVP |
| NF-SEC-04 | accessToken은 DB에 해시만 저장 | MVP |
| NF-SEC-05 | HTTPS 운영 (배포 시) | P1 |
| NF-SEC-06 | content 컬럼 실제 암호화 (AES-GCM) | P2 |
| NF-SEC-07 | API 호출 rate limiting | P2 |
| NF-SEC-08 | CORS 정책 정의 | P1 |

### 3.2 성능
| ID | 요구사항 | 우선순위 |
|---|---|---|
| NF-PERF-01 | 평균 응답 시간 < 200ms (로컬 기준) | MVP |
| NF-PERF-02 | 목록 API 페이징 (기본 20, 최대 100) | MVP |
| NF-PERF-03 | DB 인덱스: 자주 조회되는 (owner_id, ...) 컬럼 | MVP |

### 3.3 안정성
| ID | 요구사항 | 우선순위 |
|---|---|---|
| NF-REL-01 | 글로벌 예외 핸들러로 일관된 에러 응답 | MVP |
| NF-REL-02 | DTO 검증 (`@Valid`) | MVP |
| NF-REL-03 | 핵심 서비스 단위 테스트 | MVP (Sprint 4) |

### 3.4 유지보수성
| ID | 요구사항 | 우선순위 |
|---|---|---|
| NF-MAIN-01 | 계층 분리 (Controller/Service/Repository) | MVP |
| NF-MAIN-02 | DTO와 Entity 분리 | MVP |
| NF-MAIN-03 | 의미 있는 클래스/메서드 명 | MVP |
| NF-MAIN-04 | Swagger 문서 자동 생성 | MVP |
| NF-MAIN-05 | README 및 시연 시나리오 문서 | MVP (Sprint 4) |

### 3.5 확장성
| ID | 요구사항 | 우선순위 |
|---|---|---|
| NF-EXT-01 | `EncryptionService` 인터페이스로 암호화 후행 추가 가능 | MVP |
| NF-EXT-02 | `NotificationService` 인터페이스로 실제 발송 후행 추가 가능 | MVP |
| NF-EXT-03 | 자동 조건 평가 스케줄러 추가 가능한 구조 | MVP |

---

## 4. 가정 및 제약 사항

### 4.1 가정
- 단일 인스턴스 배포 (분산 환경 고려하지 않음)
- 단일 DB (PostgreSQL)
- 동시 사용자 수: 학기 시연 기준 ≤ 10명
- 자산 콘텐츠는 텍스트 중심 (실제 파일 업로드는 P2)

### 4.2 제약
- 학기 일정: 4 sprint × 1주 = 약 1개월
- 외부 서비스 의존 최소화 (실제 이메일/SMS 발송 없음)
- 모바일/프론트엔드 클라이언트는 별도 (이 프로젝트는 백엔드만)

### 4.3 범위 외
- 결제 시스템
- 실제 본인 인증 (PASS, 휴대폰 인증)
- 법적 효력 있는 디지털 유언 인증
- 다국어 지원
- 모바일 앱

---

## 5. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-05-13 | 초안 작성 |
