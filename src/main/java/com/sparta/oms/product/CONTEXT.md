# Product 도메인 컨텍스트

## 이 패키지가 하는 일
상품(Product)의 CRUD와 재고 관리를 담당한다.

## 탐색 진입점
```
ProductController   ← HTTP 요청 진입점 (GET/POST/PUT/DELETE /products)
ProductService      ← 비즈니스 로직 (도메인 규칙 적용)
ProductRepository   ← 데이터 접근 (쿼리, 원자적 재고 차감)
Product             ← 엔티티 (id, name, price, stock, deletedAt)
```

## 핵심 설계 결정 (변경 전 반드시 읽을 것)

| 결정 | 근거 문서 | 도메인 규칙 |
|---|---|---|
| 소프트 삭제 (`deletedAt`) | [ADR-0001](../../../../../docs/adr/0001-soft-delete-for-products.md) | P-2 |
| 재고 차감은 native query로 원자적 처리 | [ADR-0002](../../../../../docs/adr/0002-atomic-stock-decrease.md) | P-1 |

## 에이전트 주의사항

- `ProductRepository`에 새 쿼리 추가 시 **`deletedAt IS NULL` 조건 필수**
- 재고 수정은 반드시 `decreaseStock()` 경유 — `setStock()` 직접 호출 금지 (race condition)
- 삭제된 상품 조회 시 `ProductNotFoundException` 발생 (P-2)
- 재고 부족 시 `InsufficientStockException` 발생 (P-1)