---
title: "문서 간 연결 관계도"
type: index
domain: cross-cutting
load_level: 0
verified_at: "2026-04-15"
references_code: []
related:
  - index.md
supersedes: null
superseded_by: null
---

# 문서 간 연결 관계도 (Knowledge Graph)

에이전트가 한 문서에서 시작해 연관 문서로 탐색하는 경로를 정의한다.
화살표 방향: "읽고 나면 다음에 읽어야 할 문서"

---

## 탐색 그래프

```
                         ┌──────────────┐
                         │  CLAUDE.md   │  L0 진입점
                         │  (원칙 선언)  │
                         └──────┬───────┘
                                │
                    ┌───────────▼───────────┐
                    │     docs/index.md     │  L0 라우팅
                    │   (쿼리 기반 탐색)     │
                    └──┬────────────────┬───┘
                       │                │
           ┌───────────▼──┐      ┌──────▼──────────┐
           │ architecture │      │  agents/         │
           │ /overview.md │      │  operating-      │
           │  (L1 전체구조)│      │  system.md (L1)  │
           └──┬───────────┘      └──────────────────┘
              │
    ┌─────────┼──────────────────────────┐
    │         │                          │
    ▼         ▼                          ▼
product/  order/                  constraints/
CONTEXT   CONTEXT                 domain-rules.md
.md (L1)  .md (L1)                    (L2)
    │         │                          │
    │         │                  ┌───────┴──────────┐
    ▼         ▼                  ▼                  ▼
adr/0001  adr/0003          adr/0001           enforcement
adr/0002  (L2)              adr/0002           -map.md (L2)
(L2)                        adr/0003
    │                           │
    └───────────────────────────┘
                │
        ┌───────▼────────────┐
        │ improvements/      │  L3 (문제 발생 시만)
        │ YYYY-MM-DD-*.md    │
        └────────────────────┘
```

---

## 연결 규칙

모든 문서 간 연결은 아래 유형 중 하나로 분류된다:

| 연결 유형 | 의미 | 표기 |
|---|---|---|
| `→ implements` | 이 문서의 결정을 코드로 구현한 위치 | ADR → 코드 |
| `→ enforces` | 이 규칙을 기계적으로 강제하는 수단 | domain-rules → 테스트 |
| `→ details` | 더 자세한 내용이 있는 문서 | overview → ADR |
| `→ supersedes` | 이전 결정을 대체함 | new ADR → old ADR |
| `→ related` | 같이 읽으면 유용한 문서 | ADR ↔ CONTEXT |

---

## 문서별 연결 명세

### CLAUDE.md
```
→ details:   docs/index.md
→ details:   agents/operating-system.md
→ details:   docs/constraints/domain-rules.md
→ details:   docs/constraints/enforcement-map.md
```

### docs/architecture/overview.md
```
→ details:   src/.../product/CONTEXT.md
→ details:   src/.../order/CONTEXT.md
→ details:   docs/adr/0001-*.md
→ details:   docs/adr/0002-*.md
→ details:   docs/adr/0003-*.md
→ implements: src/main/java/com/sparta/oms/ (전체)
```

### src/.../product/CONTEXT.md
```
→ details:   docs/adr/0001-soft-delete-for-products.md
→ details:   docs/adr/0002-atomic-stock-decrease.md
→ enforces:  docs/constraints/domain-rules.md#P-1
→ enforces:  docs/constraints/domain-rules.md#P-2
→ implements: ProductRepository.java
→ implements: ProductService.java
```

### src/.../order/CONTEXT.md
```
→ details:   docs/adr/0002-atomic-stock-decrease.md
→ details:   docs/adr/0003-join-fetch-for-orders.md
→ enforces:  docs/constraints/domain-rules.md#O-1
→ enforces:  docs/constraints/domain-rules.md#O-3
→ implements: OrderRepository.java
→ implements: OrderService.java
```

### docs/adr/0001-soft-delete-for-products.md
```
→ enforces:  docs/constraints/domain-rules.md#P-2
→ implements: Product.deletedAt (필드)
→ implements: ProductRepository (모든 쿼리의 deletedAt IS NULL 조건)
→ related:   docs/adr/0002-atomic-stock-decrease.md
```

### docs/adr/0002-atomic-stock-decrease.md
```
→ enforces:  docs/constraints/domain-rules.md#P-1
→ enforces:  docs/constraints/domain-rules.md#O-1
→ implements: ProductRepository.decreaseStock()
→ related:   docs/adr/0001-soft-delete-for-products.md
```

### docs/adr/0003-join-fetch-for-orders.md
```
→ enforces:  docs/constraints/domain-rules.md#O-3
→ implements: OrderRepository (JOIN FETCH 쿼리)
→ related:   observability/logging/strategy.md (N+1 감지)
```

### docs/constraints/domain-rules.md
```
→ enforces → (각 규칙별 테스트)
→ details:  docs/adr/ (각 규칙의 근거)
→ related:  docs/constraints/enforcement-map.md
```

### docs/constraints/enforcement-map.md
```
→ implements: src/test/.../architecture/ArchitectureTest.java
→ implements: src/test/.../product/ProductTest.java
→ related:   docs/constraints/domain-rules.md
→ related:   docs/improvements/README.md
```

---

## 연결 추가 절차

새 연결이 생기는 시점:
- 새 ADR 작성 → `architecture/overview.md`와 해당 `CONTEXT.md`에 링크 추가
- 새 도메인 규칙 → `enforcement-map.md`에 항목 추가 + 해당 ADR에 `→ enforces` 추가
- 새 구현 → ADR의 `→ implements` 업데이트
- 구 결정 대체 → 기존 ADR에 `superseded_by` 설정, 새 ADR에 `supersedes` 설정