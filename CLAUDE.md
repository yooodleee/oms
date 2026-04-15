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
