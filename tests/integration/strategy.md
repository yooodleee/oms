---
title: "통합 테스트 전략"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/test/java/com/sparta/oms/product/repository/ProductRepositoryTest.java
  - src/test/java/com/sparta/oms/order/repository/OrderRepositoryTest.java
related:
  - ../unit/strategy.md
  - ../e2e/strategy.md
  - ../../docs/adr/0001-soft-delete-for-products.md
  - ../../docs/adr/0003-join-fetch-for-orders.md
supersedes: null
superseded_by: null
---

# 통합 테스트 전략

## 범위

Repository 쿼리의 실제 SQL 동작, 트랜잭션 경계, ADR 결정 사항을 실제 DB(H2 in-memory)로 검증한다.

## 단위 테스트와의 구분

| 항목 | 단위 테스트 | 통합 테스트 |
|---|---|---|
| DB | Mock | H2 in-memory |
| 검증 대상 | 비즈니스 로직 | 쿼리·트랜잭션 |
| 속도 | 빠름 | 중간 |
| ADR 검증 | ❌ | ✅ |

## 통합 테스트가 반드시 검증해야 할 ADR

```
ADR-0001: 소프트 삭제
  → findAllByDeletedAtIsNull() 은 deletedAt이 설정된 상품을 반환하지 않는다
  → findByIdAndDeletedAtIsNull() 은 삭제된 상품에 대해 empty를 반환한다

ADR-0002: 재고 차감 원자성
  → @Transactional 내에서 재고 차감 실패 시 주문이 롤백된다
  → 동시 요청에서 재고가 음수로 내려가지 않는다 (동시성 테스트)

ADR-0003: JOIN FETCH
  → findAllWithProduct()는 단일 쿼리로 Product를 함께 로딩한다
  → 페이지네이션과 함께 N+1이 발생하지 않는다
```

## 설정 구조

```java
// ProductRepositoryTest.java 예시 구조
@DataJpaTest                          // JPA 레이어만 로딩 (빠름)
@DisplayName("ProductRepository 통합 테스트")
class ProductRepositoryTest {

    @Autowired ProductRepository productRepository;

    @Test
    @DisplayName("[ADR-0001][P-2] 소프트 삭제된 상품은 목록 조회에서 제외된다")
    void deleted_product_is_excluded_from_list() { ... }

    @Test
    @DisplayName("[ADR-0001][P-2] 소프트 삭제된 상품은 단건 조회에서 empty를 반환한다")
    void deleted_product_returns_empty_on_find_by_id() { ... }
}

// OrderRepositoryTest.java 예시 구조
@DataJpaTest
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryTest {

    @Test
    @DisplayName("[ADR-0003][O-3] 주문 목록 조회는 N+1 없이 단일 쿼리로 처리된다")
    void order_list_is_fetched_without_n_plus_one() { ... }
}
```

## N+1 검증 방법

```java
// SQL 카운트 기반 N+1 검증
@Test
void order_list_is_fetched_without_n_plus_one() {
    // given: 상품 3개, 각각 주문 1건
    // when: findAllWithProduct() 실행
    // then: SQL 실행 횟수 = 1 (JOIN FETCH)

    // StatementInspector 또는 @SQL 로그 카운트로 검증
    // Hibernate Statistics 활용: sessionFactory.getStatistics()
}
```

## 현재 상태

| 테스트 | 상태 | 우선순위 |
|---|---|---|
| `ProductRepositoryTest` (ADR-0001) | ❌ MISSING | 높음 (P-2 강제) |
| `OrderRepositoryTest` (ADR-0003, N+1) | ❌ MISSING | 높음 (O-3 강제) |
| 동시성 테스트 (ADR-0002) | ❌ MISSING | 중간 |

## 실행

```bash
./gradlew test --tests "com.sparta.oms.product.repository.*"
./gradlew test --tests "com.sparta.oms.order.repository.*"
```