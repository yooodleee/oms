# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Required Output 1: Repository Structure

하네스 엔지니어링 시스템의 전체 레포지토리 구조. 에이전트는 이 구조를 탐색 진입점으로 사용한다.

```
oms/
├── CLAUDE.md                          ← 하네스 원칙 (에이전트 진입점)
├── docs/
│   ├── architecture/overview.md       ← 시스템 구조, 레이어, 기술 스택
│   ├── design/api-spec.md             ← API 명세
│   ├── product/requirements.md        ← 제품 요구사항, 우선순위
│   ├── security/security-policy.md    ← 보안 정책, 금지 행동
│   ├── reliability/sla.md             ← SLO, 장애 등급, 롤백 기준
│   ├── adr/                           ← 아키텍처 결정 근거
│   ├── constraints/
│   │   ├── domain-rules.md            ← 위반 불가 도메인 규칙
│   │   └── enforcement-map.md         ← 규칙별 강제 상태 추적
│   ├── guardrails/                    ← 제약 및 가드레일 시스템
│   │   ├── system.md                  ← 가드레일 4계층 설계 (L0)
│   │   ├── code-style.md              ← Checkstyle 규칙 (STYLE-1~4)
│   │   ├── architecture-boundaries.md ← ArchUnit 규칙 (ARCH-1~6)
│   │   ├── dependency-policy.md       ← 의존성 승인 절차
│   │   └── interface-contracts.md     ← @Valid·DTO·Repository 계약
│   └── improvements/                  ← 지속적 개선 시스템
│       ├── system.md                  ← 개선 루프 전체 설계 (L1)
│       ├── quality-metrics.md         ← 품질 측정 지표 정의
│       ├── bad-patterns.md            ← 나쁜 패턴 카탈로그 (BP-1~6)
│       ├── tech-debt-registry.md      ← 기술 부채 추적부 (TD-1~5)
│       └── refactoring-agent.md       ← 자동 리팩토링 에이전트 설계
├── plans/
│   ├── active/                        ← 진행 중인 intent 파일
│   └── completed/                     ← 완료된 intent 파일 (결과 포함)
├── agents/
│   ├── roles/                         ← 에이전트별 책임 정의
│   │   └── refactor-agent.md          ← 자동 리팩토링 에이전트 역할
│   └── workflows/feature-workflow.md  ← 신규 기능 구현 절차
├── src/
│   ├── main/java/com/sparta/oms/
│   │   ├── product/CONTEXT.md         ← 상품 도메인 탐색 가이드
│   │   └── order/CONTEXT.md           ← 주문 도메인 탐색 가이드
│   └── test/java/com/sparta/oms/
│       ├── architecture/ArchitectureTest.java  ← ARCH-1~6 강제
│       └── quality/CodeQualityTest.java        ← CQ-1~4 품질 스멜 탐지
├── tests/README.md                    ← 테스트 전략, 커버리지 목표
├── infra/
│   └── ci/gate-pipeline.yml           ← CI 게이트 설계
├── observability/
│   ├── logging/strategy.md            ← 로그 레벨 기준, N+1 감지
│   ├── metrics/strategy.md            ← 측정 지표 정의
│   └── tracing/strategy.md            ← 트레이싱 도입 기준
├── config/
│   └── checkstyle/checkstyle.xml      ← Checkstyle 규칙 파일
└── scripts/
    ├── verify-gates.sh                ← L1~L3 게이트 일괄 실행
    ├── check-guardrails.sh            ← 가드레일 4계층 일괄 실행
    └── assess-quality.sh              ← 코드 품질 자동 평가 (점수 산출)
```

**에이전트 탐색 우선순위:** `CLAUDE.md` → `docs/architecture/overview.md` → 해당 도메인 `CONTEXT.md` → `docs/constraints/`

---

## Required Output 6: Continuous Improvement System

상세 설계: `docs/improvements/system.md`

**3종 탐지 메커니즘:**

| 메커니즘 | 도구 | 탐지 대상 |
|---|---|---|
| 코드 품질 스멜 | `CodeQualityTest.java` (ArchUnit) | Entity setter, DTO 오염, 예외 계층 위반 |
| 커버리지 측정 | JaCoCo (`./gradlew jacocoTestCoverageVerification`) | 테스트 미검증 코드 경로 |
| 기술 부채 탐지 | `scripts/assess-quality.sh` | TODO/FIXME, 하드코딩 메시지, MISSING 항목 |

**자동 리팩토링 에이전트:** `agents/roles/refactor-agent.md`

```
탐지 → 우선순위 결정 → Intent 파일 생성 → 안전 변경 → 게이트 검증 → 기록
```

자동화 가능 범위: 와일드카드 import 제거, @Transactional 추가, 에러 메시지 상수화, 테스트 추가  
인간 승인 필요: 클래스 분리, 도메인 예외 계층 도입, 패키지 구조 변경

**나쁜 패턴 카탈로그:** `docs/improvements/bad-patterns.md`
- BP-1: Entity public setter (CQ-1 강제)
- BP-2: God Service (TD-5, ⚠️ PARTIAL)
- BP-3: Raw RuntimeException (CQ-3 강제)
- BP-4: DTO-Entity 혼용 (CQ-2 강제)
- BP-5: 하드코딩 에러 메시지 (TD-3, ⚠️ PARTIAL)
- BP-6: @Transactional 불일치 (TD-1, ❌ MISSING)

**기술 부채 추적:** `docs/improvements/tech-debt-registry.md`  
**품질 점수 기준:** `docs/improvements/quality-metrics.md`

**완료 기준 (Principle 7):** 코드 수정만 있고 하네스 개선 없으면 작업 미완료

---

## Required Output 5: Constraints & Guardrails System

상세 설계: `docs/guardrails/system.md`

**가드레일 4계층:**

| 계층 | 도구 | 실행 명령 | 문서 |
|---|---|---|---|
| 코드 스타일 | Checkstyle | `./gradlew checkstyleMain` | `docs/guardrails/code-style.md` |
| 아키텍처 경계 | ArchUnit | `./gradlew test --tests "*.ArchitectureTest"` | `docs/guardrails/architecture-boundaries.md` |
| 의존성 감사 | ArchUnit + 스크립트 | `scripts/check-guardrails.sh` | `docs/guardrails/dependency-policy.md` |
| 인터페이스 계약 | @Valid + Bean Validation | 런타임 강제 | `docs/guardrails/interface-contracts.md` |

**일괄 실행:** `scripts/check-guardrails.sh` — 4계층 순차 실행, 하나라도 실패 시 중단

**규칙 강제 상태 (요약):**

```
STYLE-1~4   ✅ Checkstyle (config/checkstyle/checkstyle.xml)
ARCH-1~6    ✅ ArchUnit (ArchitectureTest.java)
P-1~P-3     ✅ JUnit (ProductTest.java) + 도메인 검증
O-1         ✅ JUnit (OrderServiceTest.java)
O-2         ⚠️ PARTIAL (DELETE API 미존재로 암묵적 강제)
O-3         ⚠️ PARTIAL (JOIN FETCH 존재, SQL count 미검증)
IFACE-1     ✅ @Valid + Bean Validation (컨트롤러·DTO)
IFACE-2     ❌ MISSING (E2E 테스트 미구현)
```

전체 상태: `docs/constraints/enforcement-map.md`

**에이전트 행동 기준:**
- 새 기능 구현 전 `scripts/check-guardrails.sh` 실행
- `❌ MISSING` 항목 발견 시 구현 완료 전에 강제 수단 추가
- 가드레일 우회(suppress, ignore) 금지 — 규칙이 잘못됐다면 규칙 자체를 수정

---

## Required Output 4: Feedback & Evaluation System

상세 설계: `docs/feedback/system.md`

**테스트 계층 및 책임:**

| 계층 | 위치 | 검증 대상 | DB |
|---|---|---|---|
| 단위 | `src/test/.../entity/`, `.../service/` | 도메인 규칙 P·O | ❌ |
| 통합 | `src/test/.../repository/` | 쿼리·트랜잭션·ADR | ✅ H2 |
| E2E | `src/test/.../controller/` | API 계약·HTTP 응답 | ✅ H2 |
| 운영 | SQL 로그 + Actuator | 실시간 이상 | ✅ MySQL |

전략 문서: `tests/unit/strategy.md` · `tests/integration/strategy.md` · `tests/e2e/strategy.md`

**피드백 저장소 2종:**

```
Ephemeral (세션 내):   build/reports/tests/   ← 즉각 진단용
Persistent (세션 간):  docs/improvements/      ← 실패 패턴 누적
                       enforcement-map.md      ← 규칙 강제 상태 추적
                       plans/completed/        ← 작업별 게이트 기록
```

**피드백 재활용 경로:**
- `enforcement-map ❌ MISSING` → 다음 에이전트가 테스트 추가 의무 인식
- `docs/improvements/` → 반복 실패 패턴 사전 경고
- `plans/completed/ iterations > 2` → Plan Agent가 복잡도 높은 영역 인식
- 운영 버그 → 해당 테스트 계층에 케이스 추가 → enforcement-map 갱신

**운영 피드백:** `observability/feedback-pipeline.md` — 신호(SQL 로그·앱 로그) → 분류 → docs/improvements/ → 하네스 개선

---

## Required Output 3: Knowledge Management System

단일 문서가 아닌 모듈화된 지식 구조. 상세 설계: `docs/index.md`, `docs/knowledge-graph.md`

**점진적 로딩 프로토콜 — 에이전트는 필요한 레벨까지만 읽는다:**

```
L0 (항상)      CLAUDE.md, docs/index.md
L1 (작업시작)  architecture/overview.md, 도메인 CONTEXT.md
L2 (구현전)    domain-rules.md, enforcement-map.md, 관련 ADR
L3 (심층)      특정 ADR 전문, improvements/*.md, security, reliability
```

**문서 메타데이터 표준** (`docs/_template.md`):

```yaml
title / type / domain / load_level / verified_at / references_code / related / supersedes / superseded_by
```

- `references_code`: 이 문서가 참조하는 실제 코드 경로 — `scripts/validate-docs.sh`가 존재 여부를 검증
- `superseded_by`: 값이 있으면 해당 문서는 읽지 않는다 (구버전)

**문서 간 연결 유형** (`docs/knowledge-graph.md`):

| 유형 | 의미 |
|---|---|
| `→ implements` | 이 결정을 구현한 코드 위치 |
| `→ enforces` | 이 규칙을 강제하는 수단 |
| `→ details` | 더 자세한 내용 |
| `→ supersedes` | 이전 결정을 대체 |

**최신성 검증:** `scripts/validate-docs.sh` 실행
- `references_code`에 선언된 코드 파일 존재 여부 확인
- 내부 Markdown 링크 유효성 확인
- `verified_at` 누락 문서 경고
- `enforcement-map.md` MISSING 항목 수 보고

**문서 추가 절차:** `_template.md` frontmatter 복사 → `docs/index.md` 항목 추가 → 연관 문서 `related` 업데이트 → `validate-docs.sh` 실행

---

## Required Output 2: Agent Operating System

에이전트가 작업을 수신·수행·검증·복구하는 완전한 운영 체계. 상세 설계: `agents/operating-system.md`

**상태 머신 요약:**
```
RECEIVING → PLANNING → EXPLORING → IMPLEMENTING → TESTING → REVIEWING → PR_READY → COMPLETE
                                        ↓ (실패)         ↓ (실패)      ↓ (실패)
                                    RETRYING(최대3)   RETRYING(최대3) RETRYING(최대2)
                                        ↓ 초과              ↓ 초과         ↓ 초과
                                     ESCALATED           ESCALATED      ESCALATED
```

**작업 수신:** `plans/active/` intent 파일이 없으면 작업을 시작하지 않는다. goal·constraints·acceptance_criteria 필수.

**컨텍스트 수집:** Explore Agent가 `git status → Glob → ./gradlew test → enforcement-map → CONTEXT.md → ADR` 순서로 탐색.

**결과 검증:** `scripts/verify-gates.sh` (L1→L2→L3 순차 실행). 하나라도 실패하면 중단.

**PR 관리:** `agents/workflows/pr-workflow.md` — 브랜치 명명, PR 본문 템플릿, 차단 조건.

**리뷰 루프:** `agents/workflows/review-loop.md` — 7원칙 체크리스트, 최대 2사이클, 피드백 형식.

**실패 복구:** `agents/workflows/retry-strategy.md` — L1/L2/Review 유형별 대응, ESCALATED 보고 형식.

**에스컬레이션 조건 (인간 개입 필수):**
- 게이트 재시도 한도 초과
- 동일 에러 2회 연속 (접근 방식 변경 필요)
- constraints 간 충돌
- 외부 라이브러리 추가 필요
- domain-rules.md 자체 변경 필요

---

## Harness Engineering Principles

이 프로젝트는 AI 에이전트가 자율적으로 소프트웨어를 설계·구현·테스트·검증·개선하는 **하네스 엔지니어링 시스템**을 기반으로 운영된다.

### Principle 1: Agent-Centric Architecture

**선언:** 인간은 의도(intent)만 정의하고, 실제 구현은 AI 에이전트가 수행한다. 수동 코딩을 전제로 설계하지 않는다.

**에이전트가 자율적으로 결정하는 것:**
- 어떤 클래스/메서드를 추가·수정할지
- 쿼리 구현 방식 (JPQL / Specification / QueryDSL 등)
- 테스트 케이스 설계
- 리팩토링 범위

**에이전트가 인간의 명시적 승인을 받아야 하는 것:**
- acceptance_criteria 자체의 변경
- out_of_scope 항목 건드리기
- 데이터베이스 스키마의 파괴적 변경
- 외부 의존성(라이브러리) 추가

**Intent 표현 표준 형식:**

작업 요청 시 인간은 아래 형식으로 의도를 표현한다. 에이전트는 이 형식을 입력으로 받아 구현 전략을 자율적으로 수립한다.

```yaml
goal:        # 무엇을 달성해야 하는가
why:         # 왜 필요한가 (비즈니스 맥락)
constraints: # 깨면 안 되는 조건
acceptance_criteria: # 완료 판정 기준
out_of_scope:        # 이번 작업 범위 밖
```

### Principle 2: Environment-Centric Design

**선언:** 긴 프롬프트나 단일 지시 파일(AGENTS.md 등)에 의존하지 않는다. 환경 자체가 에이전트에게 신호를 보내도록 설계한다.

**환경 레이어 구성 (분산 원칙):**

| 레이어 | 구성 요소 | 역할 |
|---|---|---|
| 코드 구조 | `src/` 디렉토리 | 도메인 분리, 패턴을 탐색으로 파악 |
| 설정 파일 | `settings.json`, `memory/` | 훅·퍼미션, 과거 결정 근거 |
| 자동화 신호 | hooks, CronTrigger | 환경이 에이전트에게 먼저 신호 |
| 실시간 상태 | `git status`, `./gradlew test` | 현재 진실(source of truth) |

**에이전트 탐색 우선 프로토콜 — 작업 시작 시 아래 순서를 따른다:**

1. `git status`, `git log` → 현재 변경 상태 파악
2. `Glob("**/*.java")` → 실제 코드 구조 탐색
3. `./gradlew test` → 현재 품질 베이스라인 확인
4. `memory/` 파일 → 과거 결정 근거 확인
5. 이 파일(CLAUDE.md) → 목표·제약 확인

**규칙:** CLAUDE.md보다 실제 코드와 테스트 결과가 더 신뢰할 수 있는 진실이다. CLAUDE.md에는 구현 절차가 아닌 목표·제약·검증 기준만 기술한다.

### Principle 3: In-Repository Knowledge System

**선언:** 모든 컨텍스트는 레포지토리 내부에 존재해야 한다. 외부 문서, 대화 기록, 메모리 시스템에 의존하지 않는다. 레포를 클론하면 에이전트가 작동하는 데 필요한 모든 것이 거기 있어야 한다.

**저장소 내 지식 구조:**

```
oms/
├── CLAUDE.md                     ← 하네스 원칙 (이 파일)
├── docs/
│   ├── adr/                      ← Architecture Decision Records (왜 이렇게 설계했는가)
│   ├── constraints/              ← 깨면 안 되는 도메인 규칙
│   └── intents/                  ← 완료된 작업의 intent 기록
└── .claude/settings.json         ← 훅·퍼미션 (git tracked)
```

**지식 유형별 위치:**

| 지식 유형 | 저장 위치 |
|---|---|
| 아키텍처 결정 근거 | `docs/adr/NNNN-*.md` |
| 도메인 제약 규칙 | `docs/constraints/domain-rules.md` |
| 작업 의도 기록 | `docs/intents/YYYY-MM-DD-*.yaml` |
| 에이전트 행동 원칙 | `CLAUDE.md` |
| 실행 가능한 명세 | 테스트 코드 자체 |

**ADR 표준 형식** (`docs/adr/NNNN-title.md`):

```markdown
## 상태 / 맥락 / 결정 / 결과 / 에이전트 주의사항
```

**규칙:** 문서는 거짓말하지만 테스트는 거짓말하지 않는다. 도메인 규칙은 텍스트가 아닌 통과하는 테스트로 증명한다. 에이전트는 외부 메모리나 대화 기록 없이 레포만으로 컨텍스트를 파악할 수 있어야 한다.

### Principle 4: Feedback Loop-Based System

**선언:** 모든 작업은 검증 가능한 출력을 가진다. 시스템은 출력을 관찰하고, 오류를 감지하고, 스스로 수정을 시도한다. 인간은 루프가 스스로 해결하지 못할 때만 개입한다.

**검증 레이어 — 파일 수정 후 L1부터 순서대로 통과해야 한다:**

| 레이어 | 측정 수단 | 감지 대상 |
|---|---|---|
| L1: 컴파일 | `./gradlew compileJava` | 문법·타입 오류 |
| L2: 단위 테스트 | `./gradlew test` | 도메인 규칙 위반 |
| L3: 빌드 | `./gradlew build` | 전체 아티팩트 무결성 |
| L4: API 동작 | HTTP 응답 코드·본문 | 런타임 계약 위반 |
| L5: 성능 | SQL 로그 쿼리 수 | N+1, 슬로우 쿼리 |

**자기 수정 프로토콜:**
- 실패 시 원인 파일·라인 특정 → 수정 → 재검증, 최대 **3회** 반복
- L2 실패: "회귀 vs 의도된 변경" 분류 후 대응
- L5 N+1 감지: ADR-0003 참조하여 JOIN FETCH 누락 확인
- 3회 소진, 제약 충돌, 도메인 규칙 변경 필요 시 → **인간에게 에스컬레이션**

**작업 결과 기록** (`docs/intents/YYYY-MM-DD-<작업명>.yaml`):

```yaml
result:
  status: COMPLETED | FAILED | ESCALATED
  iterations: 2
  gates_passed: [L1, L2, L3]
  failure_log:
    - gate: L2
      error: "<실패 테스트명>"
      fix_applied: "<적용한 수정>"
```

### Principle 5: AI-Friendly Structure (Legibility)

**선언:** 모든 구조는 AI 에이전트가 추론 없이 탐색만으로 이해할 수 있도록 설계한다. 암묵적 관례 대신 명시적 선언을 사용한다.

**Legibility 4개 레이어:**

- **L1: 파일 위치가 역할을 말한다** — 각 디렉토리에 `CONTEXT.md`를 두어 패키지 목적·주의사항을 명시한다
- **L2: 이름이 의도를 완전히 표현한다** — `updateStock()` ❌ → `decreaseStockAtomically()` ✅ / `testOrder2()` ❌ → `order_fails_when_stock_is_zero()` ✅
- **L3: 연결이 명시적이다** — 도메인 규칙과 연관된 메서드·쿼리에 `@see docs/adr/NNNN-*.md` 참조를 추가한다
- **L4: 예외 계층이 도메인 규칙을 반영한다** — `OmsException → ProductException → InsufficientStockException` (domain-rules P-1)

**테스트 명명 기준:**

```java
@DisplayName("[규칙ID] <시나리오>는 <기대결과>를 반환한다")
void <결과>_when_<조건>() { }
// 예: @DisplayName("[P-1] 재고가 0일 때 주문 생성은 InsufficientStockException을 던진다")
```

**Legibility 자가 점검:** 새 에이전트가 레포를 클론했을 때 파일명·위치·이름만으로 "이것은 무엇인가?"에 답할 수 있으면 통과다.

### Principle 6: Enforced Constraints

**선언:** 모든 규칙은 기계적으로 강제되어야 한다. 강제 수단이 없는 규칙은 규칙이 아니라 메모다. "권장 사항" 수준의 규칙은 이 시스템에서 허용하지 않는다.

**강제 계층 (빠를수록 비용이 낮다):**

```
컴파일 타임  →  ArchUnit (레이어 의존성 방향, 명명 규칙)
테스트 타임  →  JUnit (도메인 규칙 P-1~3, O-1~3)
커밋 타임    →  Pre-commit hook (테스트 미통과 시 차단)
(없음)       →  "권장 사항" 문서 — 금지
```

**강제 상태 기준 (`docs/constraints/enforcement-map.md` 참조):**

| 상태 | 의미 |
|---|---|
| ✅ ENFORCED | 자동 검증 수단 존재 |
| ⚠️ PARTIAL | 일부 케이스만 강제, 보완 필요 |
| ❌ MISSING | 강제 수단 없음 — merge 금지 |

**에이전트 행동 규칙:**
- 새 기능 구현 시 관련 도메인 규칙이 `❌ MISSING`이면, 구현 완료 전에 강제 수단을 함께 추가한다
- 규칙을 강제할 수 없으면 규칙을 더 작은 단위로 분해하거나, 분해도 불가능하면 규칙을 삭제한다
- 아키텍처 규칙은 `src/test/java/com/sparta/oms/architecture/ArchitectureTest.java` (ArchUnit)으로 강제된다

### Principle 7: Continuous Improvement

**선언:** 문제 발생 시 코드 수정(증상 치료)만으로 완료하지 않는다. 하네스 자체가 개선되어 동일 문제가 구조적으로 재발하지 않도록 한다.

**하네스 개선 트리거:**
- **버그 탈출**: 기존 게이트(L1~L5)를 통과한 버그 발견 → 누락된 게이트 추가
- **반복 실수**: 에이전트가 같은 유형의 실수를 두 번 이상 반복 → 구조적 차단
- **MISSING 해소**: `enforcement-map.md`의 `❌ MISSING` 항목 → 기능 구현 시 함께 `✅ ENFORCED`로 전환

**하네스 개선 루프 — 문제 발생 시 반드시 이 순서를 따른다:**

1. 즉각 수정 (코드·설정 변경)
2. 근본 원인 분석 ("어떤 레이어가 이것을 놓쳤는가?")
3. 하네스 수정 (새 테스트 / ArchUnit 규칙 / ADR 중 하나 이상)
4. 개선 기록 (`docs/improvements/YYYY-MM-DD-<이슈명>.md`)
5. `enforcement-map.md` 상태 갱신

**개선 기록 형식** (`docs/improvements/YYYY-MM-DD-<이슈명>.md`):

```markdown
## 발생한 문제 / 탈출 경로 분석 / 즉각 수정 / 하네스 개선 / 재발 방지 확인
```

**완료 기준:** 코드 수정만 있고 하네스 개선이 없으면 작업이 완료된 것이 아니다.

---

## Commands

```bash
# Build
./gradlew build

# Run application (port 8084)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.sparta.oms.OmsApplicationTests"

# Clean build
./gradlew clean build
```

## Environment Variables

The application requires these environment variables to start:

```
DATABASE_URL=jdbc:mysql://localhost:3306/<dbname>
DATABASE_USERNAME=<user>
DATABASE_PASSWORD=<password>
```

## Architecture

This is a Spring Boot 4 / Java 17 Order Management System with two domains under `com.sparta.oms`:

**Layered structure per domain:** `controller → service → repository → entity`, with DTOs for API contracts.

### Product domain (`com.sparta.oms.product`)
CRUD for products (name, price, stock). Stock is managed atomically via a native `@Modifying` query in `ProductRepository.decreaseStock()` to prevent race conditions during concurrent orders.

### Order domain (`com.sparta.oms.order`)
Order creation deducts stock from the product atomically. Order list endpoint supports pagination via `page`/`size` query params. `OrderRepository` uses `JOIN FETCH` to eagerly load the related `Product` and avoid N+1 queries.

### Key design notes
- Schema is auto-managed by Hibernate (`ddl-auto=update`) — no migration files.
- `Order` entity has a `@ManyToOne` FK to `Product`.
- SQL logging is enabled (`show-sql=true`).
- Server port: **8084**.
