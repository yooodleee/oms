---
title: "단위 테스트 전략"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/test/java/com/sparta/oms/product/entity/ProductTest.java
  - src/test/java/com/sparta/oms/order/entity/OrderTest.java
  - src/test/java/com/sparta/oms/product/service/ProductServiceTest.java
  - src/test/java/com/sparta/oms/order/service/OrderServiceTest.java
related:
  - ../integration/strategy.md
  - ../e2e/strategy.md
  - ../../docs/constraints/domain-rules.md
  - ../../docs/constraints/enforcement-map.md
supersedes: null
superseded_by: null
---

# 단위 테스트 전략

## 범위

도메인 규칙(P-1~3, O-1~3)과 서비스 비즈니스 로직을 DB 없이 검증한다.
실제 의존성은 Mockito로 대체한다.

## 테스트 레이어

```
Entity Tests      → 도메인 로직 단독 검증 (Mockito 불필요)
  product/entity/ProductTest.java
  order/entity/OrderTest.java

Service Tests     → 비즈니스 흐름 검증 (Repository Mockito 처리)
  product/service/ProductServiceTest.java
  order/service/OrderServiceTest.java

Architecture Tests → 레이어 의존성 강제 (ArchUnit)
  architecture/ArchitectureTest.java
```

## 명명 기준 (Principle 5)

```java
// 형식: [규칙ID] 시나리오 → 기대결과
@DisplayName("[P-1] 재고보다 많은 수량을 주문하면 예외가 발생한다")
void throws_exception_when_order_quantity_exceeds_stock() { }

// 성공 케이스도 규칙 ID를 붙인다
@DisplayName("[O-1] 주문 생성 성공 시 재고가 차감된다")
void stock_decreases_when_order_is_created_successfully() { }
```

## 도메인 규칙별 테스트 매핑

| 규칙 ID | 검증 대상 | 테스트 파일 | 상태 |
|---|---|---|---|
| P-1 | 재고 음수 금지 | `ProductTest` | ✅ |
| P-2 | 삭제된 상품 조회 제외 | `ProductServiceTest` | ✅ |
| P-3 | 가격·재고 0 이상 | `ProductTest` | ❌ MISSING |
| O-1 | 주문·재고 차감 원자성 | `OrderServiceTest` | ✅ |
| O-2 | 주문 삭제 불가 | (DELETE API 미존재) | ⚠️ PARTIAL |
| O-3 | N+1 금지 | 통합 테스트에서 검증 | → integration |

## 실행

```bash
./gradlew test --tests "com.sparta.oms.product.entity.*"
./gradlew test --tests "com.sparta.oms.product.service.*"
./gradlew test --tests "com.sparta.oms.order.*"
./gradlew test --tests "com.sparta.oms.architecture.*"
```
