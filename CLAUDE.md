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
