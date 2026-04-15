# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
