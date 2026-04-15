---
title: "ADR-0001: 상품 소프트 삭제 도입"
type: adr
domain: product
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/main/java/com/sparta/oms/product/entity/Product.java
  - src/main/java/com/sparta/oms/product/repository/ProductRepository.java
related:
  - 0002-atomic-stock-decrease.md
  - ../constraints/domain-rules.md
supersedes: null
superseded_by: null
---

# ADR-0001: 상품 소프트 삭제 도입

## 상태
Accepted

## 맥락
상품을 물리적으로 삭제하면 해당 상품을 참조하는 기존 주문 이력의 무결성이 깨진다.
`Order` 엔티티는 `@ManyToOne`으로 `Product`를 참조하므로, 상품 삭제 시 FK 제약 위반이 발생하거나
주문 이력에서 상품 정보를 조회할 수 없게 된다.

## 결정
`Product` 엔티티에 `deletedAt` 필드를 추가하고, 삭제 요청 시 해당 필드에 타임스탬프를 기록한다.
물리적 DELETE는 수행하지 않는다.

## 결과
- 주문 이력은 삭제된 상품 정보를 계속 조회할 수 있다
- 상품 목록 조회 쿼리는 반드시 `deletedAt IS NULL` 조건을 포함해야 한다
- 재고 차감 등 상품 관련 쓰기 작업은 삭제된 상품에 대해 거부해야 한다

## 에이전트 주의사항
- `ProductRepository`에 새 쿼리를 추가할 때 `deletedAt IS NULL` 조건 누락 금지
- 상품 관련 API 응답에 삭제된 상품이 포함되면 버그로 간주한다
- 이 결정을 번복하려면 기존 주문 이력 마이그레이션 계획이 먼저 수립되어야 한다