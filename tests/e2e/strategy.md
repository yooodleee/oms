---
title: "E2E 테스트 전략"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/test/java/com/sparta/oms/product/controller/ProductControllerTest.java
  - src/test/java/com/sparta/oms/order/controller/OrderControllerTest.java
related:
  - ../unit/strategy.md
  - ../integration/strategy.md
  - ../../docs/design/api-spec.md
supersedes: null
superseded_by: null
---

# E2E 테스트 전략

## 범위

`docs/design/api-spec.md`에 선언된 API 계약을 HTTP 레벨에서 검증한다.
MockMvc를 사용하여 실제 서버 기동 없이 전체 요청-응답 흐름을 테스트한다.

## 단위·통합 테스트와의 구분

| 항목 | E2E 테스트 |
|---|---|
| 검증 대상 | HTTP 상태 코드, 응답 바디 형식, 에러 메시지 |
| 도구 | `@SpringBootTest` + `MockMvc` |
| 근거 문서 | `docs/design/api-spec.md` |

## API 계약 검증 대상

```
docs/design/api-spec.md 기반:

Product API:
  GET  /products           → 200 + 배열 반환
  GET  /products/{id}      → 200 / 404 (삭제된 상품)
  POST /products           → 201 + 생성된 상품
  PUT  /products/{id}      → 200 / 404
  DELETE /products/{id}    → 204 (소프트 삭제)

Order API:
  POST /orders             → 201 / 400 (재고 부족) / 404 (없는 상품)
  GET  /orders?page=0&size=10 → 200 + 페이지네이션 구조
```

## 설정 구조

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Product API E2E 테스트")
class ProductControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("[api-spec] DELETE /products/{id}는 소프트 삭제 후 204를 반환한다")
    void delete_returns_204_and_soft_deletes() throws Exception {
        // given: 상품 생성
        // when: DELETE /products/{id}
        // then: 204 반환
        //       GET /products/{id} → 404 (소프트 삭제 확인)
    }

    @Test
    @DisplayName("[P-1][api-spec] 재고 초과 주문은 400을 반환한다")
    void order_with_insufficient_stock_returns_400() throws Exception {
        // given: stock=1인 상품
        // when: POST /orders {quantity: 5}
        // then: 400 반환
    }
}
```

## E2E 피드백 → API 스펙 갱신 연동

```
E2E 테스트 실패
    → api-spec.md와 실제 응답 비교
    → 불일치가 구현 버그이면: 구현 수정
    → 불일치가 스펙 변경이면: api-spec.md 먼저 갱신 후 구현
    (스펙이 코드보다 항상 우선)
```

## 현재 상태

| 테스트 | 상태 | 우선순위 |
|---|---|---|
| `ProductControllerTest` | ❌ MISSING | 중간 |
| `OrderControllerTest` | ❌ MISSING | 중간 |

## 실행

```bash
./gradlew test --tests "com.sparta.oms.product.controller.*"
./gradlew test --tests "com.sparta.oms.order.controller.*"
```
