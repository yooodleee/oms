# ADR-0002: 재고 차감 원자성 보장

## 상태
Accepted

## 맥락
동시에 여러 주문이 들어올 경우, JPA의 엔티티 조회 → 수정 → 저장 패턴은 race condition을 유발한다.
두 트랜잭션이 동시에 재고를 읽으면 둘 다 차감에 성공하여 재고가 음수가 될 수 있다.

## 결정
`ProductRepository.decreaseStock()`을 `@Modifying` native query로 구현한다.
```sql
UPDATE product SET stock = stock - :quantity WHERE id = :id AND stock >= :quantity
```
업데이트된 행이 0이면 재고 부족으로 판단하여 예외를 던진다.

## 결과
- 재고 차감은 DB 레벨에서 원자적으로 처리된다
- 애플리케이션 레벨에서 재고를 읽고 비교하는 코드를 추가해서는 안 된다
- 이 쿼리가 수정될 경우 반드시 동시성 테스트를 통과해야 한다

## 에이전트 주의사항
- 재고 관련 로직을 변경할 때 `decreaseStock()`을 우회하는 구현 금지
- `product.setStock(product.getStock() - quantity)` 패턴은 race condition을 유발하므로 절대 사용 금지
- 재고 차감 변경 시 acceptance criteria에 "동시 주문 N건에서 재고 초과 없음" 조건을 포함해야 한다