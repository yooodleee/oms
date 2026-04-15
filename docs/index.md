---
title: "OMS 지식 시스템 마스터 인덱스"
type: index
domain: cross-cutting
load_level: 0
verified_at: "2026-04-15"
references_code: []
related:
  - architecture/overview.md
  - constraints/domain-rules.md
supersedes: null
superseded_by: null
---

# OMS 지식 시스템 마스터 인덱스

에이전트는 이 파일을 `CLAUDE.md` 다음으로 읽는다.
"무엇을 알아야 하는가?" → "어떤 문서를 읽어야 하는가?" 를 라우팅한다.

---

## 점진적 로딩 맵

에이전트는 L0부터 시작해 필요한 레벨까지만 로딩한다.

```
L0 (항상)          L1 (작업 시작)         L2 (구현 전)              L3 (심층)
─────────────      ──────────────────     ─────────────────────     ──────────────────
CLAUDE.md     →   architecture/          domain-rules.md       →   adr/0001-*.md
docs/index.md     overview.md            enforcement-map.md        adr/0002-*.md
                  product/CONTEXT.md     관련 ADR (L2 쿼리 참조)    adr/0003-*.md
                  order/CONTEXT.md                                 improvements/*.md
                                                                   security/policy.md
                                                                   reliability/sla.md
```

---

## 쿼리 기반 라우팅 (Query → Document)

"~할 때 무엇을 읽는가?"

### 작업 시작 시

| 상황 | 읽어야 할 문서 | 레벨 |
|---|---|---|
| 새 작업을 시작한다 | `docs/index.md` → `architecture/overview.md` | L0→L1 |
| Product 도메인 변경 | `src/.../product/CONTEXT.md` | L1 |
| Order 도메인 변경 | `src/.../order/CONTEXT.md` | L1 |
| 도메인 규칙을 확인한다 | `constraints/domain-rules.md` | L2 |
| 규칙이 강제되는지 확인한다 | `constraints/enforcement-map.md` | L2 |

### 구현 중

| 상황 | 읽어야 할 문서 | 레벨 |
|---|---|---|
| `ProductRepository` 수정 | `adr/0001-soft-delete.md` + `adr/0002-atomic-stock.md` | L2 |
| `OrderRepository` 수정 | `adr/0003-join-fetch-for-orders.md` | L2 |
| 재고 차감 로직 변경 | `adr/0002-atomic-stock-decrease.md` | L2 |
| 새 쿼리 작성 | `constraints/domain-rules.md` + `adr/0001` | L2 |
| 예외 처리 추가 | `security/security-policy.md` | L3 |
| API 응답 형식 변경 | `design/api-spec.md` | L2 |

### 테스트·검증 중

| 상황 | 읽어야 할 문서 | 레벨 |
|---|---|---|
| 테스트 실패 분석 | `agents/workflows/retry-strategy.md` | L2 |
| 테스트 케이스 설계 | `constraints/domain-rules.md` + `tests/README.md` | L2 |
| N+1 발생 감지 | `adr/0003-join-fetch-for-orders.md` + `observability/logging/strategy.md` | L2 |

### 문제 발생 시

| 상황 | 읽어야 할 문서 | 레벨 |
|---|---|---|
| 버그 발견 | `agents/workflows/retry-strategy.md` + `docs/improvements/README.md` | L2 |
| 하네스 개선 필요 | `constraints/enforcement-map.md` + `docs/improvements/README.md` | L2 |
| 장애 대응 | `reliability/sla.md` | L3 |

### PR·완료 단계

| 상황 | 읽어야 할 문서 | 레벨 |
|---|---|---|
| PR 생성 | `agents/workflows/pr-workflow.md` | L2 |
| 리뷰 진행 | `agents/workflows/review-loop.md` | L2 |
| 결과 기록 | `plans/README.md` + `docs/improvements/README.md` | L2 |

---

## 전체 문서 목록 (타입·레벨·도메인 태그)

| 경로 | 타입 | 레벨 | 도메인 | 최신성 |
|---|---|---|---|---|
| `CLAUDE.md` | index | L0 | cross-cutting | 수시 갱신 |
| `docs/index.md` | index | L0 | cross-cutting | 문서 추가 시 |
| `docs/architecture/overview.md` | architecture | L1 | cross-cutting | 구조 변경 시 |
| `docs/design/api-spec.md` | design | L2 | cross-cutting | API 변경 시 |
| `docs/product/requirements.md` | product | L1 | cross-cutting | 요구사항 변경 시 |
| `docs/security/security-policy.md` | security | L3 | cross-cutting | 보안 정책 변경 시 |
| `docs/reliability/sla.md` | reliability | L3 | cross-cutting | SLO 변경 시 |
| `docs/constraints/domain-rules.md` | constraint | L2 | cross-cutting | 규칙 변경 시 |
| `docs/constraints/enforcement-map.md` | constraint | L2 | cross-cutting | 구현 완료 시마다 |
| `docs/adr/0001-soft-delete-for-products.md` | adr | L2 | product | 결정 번복 시 |
| `docs/adr/0002-atomic-stock-decrease.md` | adr | L2 | product | 결정 번복 시 |
| `docs/adr/0003-join-fetch-for-orders.md` | adr | L2 | order | 결정 번복 시 |
| `docs/improvements/README.md` | improvement | L2 | cross-cutting | 개선 발생 시 |
| `src/.../product/CONTEXT.md` | context | L1 | product | 도메인 변경 시 |
| `src/.../order/CONTEXT.md` | context | L1 | order | 도메인 변경 시 |
| `agents/operating-system.md` | workflow | L1 | harness | AOS 변경 시 |
| `agents/workflows/pr-workflow.md` | workflow | L2 | harness | PR 정책 변경 시 |
| `agents/workflows/review-loop.md` | workflow | L2 | harness | 리뷰 정책 변경 시 |
| `agents/workflows/retry-strategy.md` | workflow | L2 | harness | 복구 전략 변경 시 |
| `tests/README.md` | constraint | L2 | cross-cutting | 테스트 전략 변경 시 |
| `observability/logging/strategy.md` | architecture | L3 | cross-cutting | 로그 정책 변경 시 |

---

## 문서 추가 절차

새 문서를 추가할 때:
1. `docs/_template.md`의 frontmatter를 복사하여 메타데이터를 채운다
2. 이 인덱스의 "전체 문서 목록"에 항목을 추가한다
3. 관련 문서의 `related` 목록에 새 문서를 추가한다
4. `scripts/validate-docs.sh` 실행하여 링크 유효성 확인
