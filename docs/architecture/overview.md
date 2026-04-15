# 시스템 아키텍처 개요

## 시스템 목적
OMS(Order Management System)는 상품 등록·관리, 주문 생성, 재고 차감을 처리하는 백엔드 서비스다.

## 레이어 구조

```
HTTP 요청
    ↓
Controller   → 요청 파싱, 응답 직렬화 (DTO 변환)
    ↓
Service      → 비즈니스 로직, 트랜잭션 경계
    ↓
Repository   → 데이터 접근 (JPA + native query)
    ↓
Database (MySQL)
```

의존성 방향: Controller → Service → Repository (역방향 금지, ARCH-1·2 강제)

## 도메인 구성

```
com.sparta.oms
├── product/    상품 CRUD + 재고 관리  →  docs/adr/0001, 0002
└── order/      주문 생성 + 목록 조회  →  docs/adr/0002, 0003
```

## 기술 스택

| 항목 | 선택 |
|---|---|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4 |
| ORM | Spring Data JPA (Hibernate) |
| DB | MySQL |
| 빌드 | Gradle |
| 포트 | 8084 |

## 핵심 설계 결정

| 결정 | 문서 |
|---|---|
| 상품 소프트 삭제 | [ADR-0001](../adr/0001-soft-delete-for-products.md) |
| 재고 차감 원자성 | [ADR-0002](../adr/0002-atomic-stock-decrease.md) |
| 주문 조회 N+1 방지 | [ADR-0003](../adr/0003-join-fetch-for-orders.md) |

## 에이전트 탐색 진입점

새 기능 구현 전 순서:
1. 이 파일 → 전체 구조 파악
2. 해당 도메인의 `CONTEXT.md` → 도메인별 주의사항
3. `docs/constraints/domain-rules.md` → 위반 불가 규칙
4. `docs/constraints/enforcement-map.md` → 현재 강제 상태
