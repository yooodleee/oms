# Order 도메인 컨텍스트

## 이 패키지가 하는 일
주문(Order) 생성과 목록 조회를 담당한다. 주문 생성 시 Product 재고를 원자적으로 차감한다.

## 탐색 진입점
```
OrderController     ← HTTP 요청 진입점 (POST /orders, GET /orders)
OrderService        ← 비즈니스 로직 (재고 차감 + 주문 생성 트랜잭션)
OrderRepository     ← 데이터 접근 (JOIN FETCH로 N+1 방지)
Order               ← 엔티티 (id, product, quantity, price, createdAt)
```

## 핵심 설계 결정 (변경 전 반드시 읽을 것)

| 결정 | 근거 문서 | 도메인 규칙 |
|---|---|---|
| 주문 생성 = 재고 차감 + Order 저장 (단일 트랜잭션) | [ADR-0002](../../../../../docs/adr/0002-atomic-stock-decrease.md) | O-1 |
| 목록 조회는 JOIN FETCH (N+1 방지) | [ADR-0003](../../../../../docs/adr/0003-join-fetch-for-orders.md) | O-3 |
| 주문은 삭제하지 않음 | [domain-rules O-2](../../../../../docs/constraints/domain-rules.md) | O-2 |

## 에이전트 주의사항

- `OrderRepository`에 새 조회 메서드 추가 시 **JOIN FETCH 포함 필수** (N+1 금지)
- 주문 생성은 `@Transactional` 범위 내에서 재고 차감과 함께 처리
- 재고 차감 실패(`InsufficientStockException`) 시 주문 생성 롤백 — 별도 보상 로직 불필요
- 페이지네이션 파라미터: `page` (0-based), `size` (기본값은 서비스 레이어에서 정의)