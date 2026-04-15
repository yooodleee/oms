---
title: "피드백 및 평가 시스템"
type: architecture
domain: cross-cutting
load_level: 1
verified_at: "2026-04-15"
references_code:
  - src/test/java/com/sparta/oms/product/entity/ProductTest.java
  - src/test/java/com/sparta/oms/order/entity/OrderTest.java
  - src/test/java/com/sparta/oms/architecture/ArchitectureTest.java
related:
  - ../constraints/enforcement-map.md
  - ../improvements/README.md
  - ../../tests/unit/strategy.md
  - ../../tests/integration/strategy.md
  - ../../tests/e2e/strategy.md
  - ../../observability/feedback-pipeline.md
supersedes: null
superseded_by: null
---

# 피드백 및 평가 시스템

에이전트가 생성한 코드가 올바른지 검증하고, 검증 결과를 수집·저장·재활용하는 완전한 피드백 체계.

---

## 피드백 계층 구조

```
                    ┌─────────────────────────────────────────────────────┐
                    │            FEEDBACK & EVALUATION SYSTEM              │
                    │                                                     │
  코드 변경         │  L1: 단위 테스트    도메인 규칙 위반 감지            │
      │             │  L2: 통합 테스트    실제 DB 동작 검증               │
      ▼             │  L3: E2E 테스트     API 계약 검증                   │
  피드백 수집 ──────│  L4: 운영 모니터링  실시간 이상 감지                 │
      │             │                                                     │
      ▼             └──────────────────────────┬──────────────────────────┘
  피드백 저장                                  │
      │              ┌──────────────┐          │  ┌──────────────────┐
      │              │  Ephemeral   │◀─────────┘  │   Persistent     │
      │              │  build/      │             │   (In-Repo)      │
      │              │  reports/    │             │                  │
      │              └──────────────┘             │ enforcement-map  │
      │                                           │ improvements/    │
      ▼                                           │ plans/completed/ │
  피드백 재활용                                   └─────────┬────────┘
                                                           │
                          다음 에이전트 세션 ◀─────────────┘
                          (과거 실패 패턴 참조)
```

---

## 테스트 계층별 책임

| 계층 | 검증 대상 | 속도 | DB 필요 | 도구 |
|---|---|---|---|---|
| 단위 테스트 | 도메인 로직, 비즈니스 규칙 | ⚡ 빠름 | ❌ | JUnit5 + Mockito |
| 통합 테스트 | Repository 쿼리, 트랜잭션 | 🔄 중간 | ✅ H2 | @SpringBootTest |
| E2E 테스트 | API 계약, HTTP 응답 | 🐢 느림 | ✅ H2 | MockMvc |
| 운영 모니터링 | 실시간 이상 감지 | ♾️ 지속 | ✅ MySQL | 로그 + Actuator |

---

## 피드백 수집 (Collection)

### 자동 수집 (에이전트가 항상 실행)

```
게이트 실행 시:
  ./gradlew test
    → build/reports/tests/test/index.html  (HTML 리포트)
    → build/reports/tests/test/xml/        (JUnit XML — CI 파싱용)
    → 콘솔 출력: PASS/FAIL 테스트 목록

수동 트리거:
  ./scripts/verify-gates.sh               (L1~L3 일괄)
  ./scripts/validate-docs.sh              (문서 최신성)
```

### 운영 수집 (지속적)

```
SQL 로그 (show-sql=true):
  → N+1 패턴 감지 (동일 쿼리 반복)
  → 슬로우 쿼리 감지

애플리케이션 로그:
  → IllegalArgumentException 빈도 (P-1, P-2 위반 시도)
  → 예외 패턴 → docs/improvements/ 기록 트리거

Actuator (미구성, 추후):
  → HTTP 응답 시간
  → 에러율
```

---

## 피드백 저장 (Storage)

### Ephemeral Storage (세션 내)

```
build/reports/tests/          ← 현재 실행 결과 (git ignore)
  test/index.html             ← 사람이 읽는 리포트
  test/xml/*.xml              ← CI가 파싱하는 리포트

용도: 현재 세션 내 즉각 진단
한계: 다음 에이전트 세션에서 사용 불가
```

### Persistent Storage (레포 내, 세션 간 공유)

```
docs/constraints/enforcement-map.md
  → 규칙별 강제 상태 (✅/⚠️/❌)
  → 어떤 규칙이 아직 테스트로 보호되지 않는가

docs/improvements/YYYY-MM-DD-*.md
  → 실패 원인 분석 + 하네스 개선 내역
  → 다음 에이전트가 같은 실수를 피하는 근거

plans/completed/YYYY-MM-DD-*.yaml
  → 작업별 게이트 통과 기록, 재시도 횟수
  → 특정 작업 유형의 실패 패턴 추적

tests/README.md (커버리지 테이블)
  → 현재 테스트 커버리지 현황
  → MISSING 항목이 다음 구현 시 작업 범위에 포함
```

---

## 피드백 재활용 (Re-utilization)

### 즉시 재활용 (현재 세션)

```
테스트 실패 발생
    ↓
agents/workflows/retry-strategy.md 참조
    → 유형 분류 (회귀 / 의도된 변경 / 환경 문제)
    → 유형별 대응 실행
    → 최대 3회 재시도
```

### 세션 간 재활용 (다음 에이전트)

```
enforcement-map.md 의 ❌ MISSING 항목
    ↓
새 기능 구현 시 에이전트가 읽음 (L2 컨텍스트 로딩)
    → "이 규칙은 아직 테스트로 보호되지 않는다"
    → 구현과 동시에 테스트 추가 의무 발생 (Principle 6)

docs/improvements/ 의 실패 패턴
    ↓
Explore Agent가 탐색 시 발견
    → "이 유형의 변경은 과거에 P-2 위반을 유발했다"
    → 사전 경고 및 추가 검증 수행

plans/completed/ 의 iterations > 2 기록
    ↓
동일 도메인 작업 시 Plan Agent가 참조
    → "이 영역은 복잡도가 높다, 더 세밀한 계획 필요"
```

### 하네스 자체 개선 (Principle 7 연동)

```
반복 실패 패턴 감지
    ↓
agents/workflows/retry-strategy.md 업데이트
    → 새 실패 유형과 대응 전략 추가

coverage gap 발견
    ↓
tests/README.md 커버리지 테이블 업데이트
enforcement-map.md MISSING → ENFORCED 전환
    → 다음 에이전트가 gap을 인식하고 채움
```
