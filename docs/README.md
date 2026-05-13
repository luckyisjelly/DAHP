# DAHP 문서

Sprint 1 (요구사항 분석 + 기본 설계)의 산출물.

## 문서 목록

| # | 문서 | 설명 |
|---|---|---|
| 00 | [결정사항](00-decisions.md) | 기술 스택, 일정, 아키텍처 방향, MVP 범위 |
| 01 | [요구사항](01-requirements.md) | 기능/비기능 요구사항, 가정 및 제약 |
| 02 | [사용자 시나리오](02-user-scenarios.md) | 5개 시나리오 (해피 패스, 권한, 비정상, 체크인, 시연) |
| 03 | [ERD](03-erd.md) | 9개 엔티티 데이터 모델 |
| 04 | [API 명세](04-api-list.md) | 28개 엔드포인트 + 에러 코드 |
| 05 | [패키지 구조](05-project-structure.md) | 백엔드 패키지 트리 |
| 06 | [화면 흐름도](06-wireframe.md) | 클라이언트 흐름 + 시퀀스 다이어그램 |

## 읽는 순서 권장

처음 보는 사람:
1. 00-decisions → 02-user-scenarios → 01-requirements → 06-wireframe → 04-api-list → 03-erd → 05-project-structure

구현 들어가기 전:
1. 00-decisions (스택 확인) → 05-project-structure (폴더 만들기) → 03-erd (엔티티 작성) → 04-api-list (컨트롤러 작성)

## 변경 정책

- 코드 변경 전 관련 문서 먼저 수정
- 각 문서 하단 변경 이력에 기록
- 큰 결정은 00-decisions.md 갱신
