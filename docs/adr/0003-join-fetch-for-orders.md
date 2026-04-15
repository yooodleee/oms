---
title: "ADR-0003: 주문 조회 JOIN FETCH"
type: adr
domain: order
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/main/java/com/sparta/oms/order/repository/OrderRepository.java
related:
  - ../constraints/domain-rules.md
  - ../../observability/logging/strategy.md
supersedes: null
superseded_by: null
---

# ADR-0003: 주문 조회 JOIN FETCH

## 상태
Accepted

## 맥락
`Order` 엔티티는 `@ManyToOne`으로 `Product`를 참조한다.
기본 지연 로딩(LAZY) 설정에서 주문 목록을 조회하면 각 주문마다 Product 조회 쿼리가 추가로 발생한다(N+1 문제).
100건의 주문 조회 시 101번의 쿼리가 실행된다.

## 결정
`OrderRepository`의 목록 조회 쿼리에 `JOIN FETCH o.product`를 사용하여 단일 쿼리로 처리한다.

## 결과
- 주문 목록은 항상 Product 정보와 함께 단일 쿼리로 조회된다
- 주문 조회에 새 연관 엔티티가 추가될 경우 JOIN FETCH 대상에 포함해야 한다
- 페이지네이션과 JOIN FETCH를 함께 사용할 경우 Hibernate 경고(`HHH90003004`)에 유의한다

## 에이전트 주의사항
- `OrderRepository`에 새 조회 메서드를 추가할 때 JOIN FETCH 없이 연관 엔티티를 접근하는 코드 금지
- 새 연관 관계 추가 시 N+1 발생 여부를 SQL 로그(`show-sql=true`)로 반드시 확인한다
- 페이지네이션 + 컬렉션 JOIN FETCH 조합은 메모리 페이지네이션 문제를 유발하므로 별도 count 쿼리 전략을 사용한다