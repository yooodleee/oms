---
title: "아키텍처 경계 규칙"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/test/java/com/sparta/oms/architecture/ArchitectureTest.java
related:
  - system.md
  - dependency-policy.md
  - ../../docs/architecture/overview.md
supersedes: null
superseded_by: null
---

# 아키텍처 경계 규칙 (ArchUnit)

**강제 수단:** `./gradlew test --tests "*.ArchitectureTest"` — 위반 시 빌드 실패

---

## 허용되는 의존성 방향

```
Controller ──▶ Service ──▶ Repository ──▶ Entity
    │                          │
    ▼                          ▼
   DTO                       Domain

Order.Service ──▶ Product.Repository  (허용: 재고 차감 필요)
Order.Service ──▶ Product.Service     (금지: ARCH-3)
Product ──▶ Order                     (금지: ARCH-3)
```

---

## 규칙별 위반 탐지·수정

### ARCH-1: Controller → Repository 직접 의존 금지
```
위반 탐지:
  ArchUnit: "ARCH-1 failed: ProductController depends on ProductRepository"

원인: Controller가 Repository를 @Autowired
수정: Controller → Service → Repository 경로로 변경
```

### ARCH-3: 도메인 격리
```
위반 탐지:
  ArchUnit: "product.service depends on order.entity"

원인: ProductService가 Order 클래스를 import
수정: 
  - Product 도메인에서 Order 참조 제거
  - 필요하다면 공유 인터페이스 또는 이벤트 패턴 도입 (인간 승인 필요)
```

### ARCH-4: DTO 위치 위반
```
위반 탐지:
  ArchUnit: "ProductResponseDto should reside in ..dto.. but resides in ..service.."

원인: DTO 클래스가 dto 패키지 외부에 생성됨
수정: dto 패키지로 이동 후 import 정리
```

### ARCH-5: 애너테이션 누락
```
위반 탐지:
  ArchUnit: "OrderService is not annotated with @Service"

원인: @Service 애너테이션 누락
수정: @Service 추가
```

---

## 경계 예외 처리 절차

경계 위반이 불가피하다고 판단될 경우:
1. 기존 경계 규칙의 타당성을 재검토한다
2. 경계를 변경해야 한다면 ADR을 작성하고 인간의 승인을 받는다
3. `ArchitectureTest.java`의 해당 규칙을 수정한다
4. `docs/constraints/enforcement-map.md`를 갱신한다

**ArchUnit 규칙을 suppress하거나 삭제하는 것은 금지된다.**