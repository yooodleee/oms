---
title: "자동 리팩토링 에이전트 설계"
type: architecture
domain: cross-cutting
load_level: 2
verified_at: "2026-04-16"
references_code:
  - scripts/assess-quality.sh
  - agents/roles/refactor-agent.md
  - plans/active/
related:
  - system.md
  - bad-patterns.md
  - tech-debt-registry.md
  - ../../agents/roles/refactor-agent.md
  - ../../agents/operating-system.md
supersedes: null
superseded_by: null
---

# 자동 리팩토링 에이전트 설계

코드 품질 평가 결과를 입력으로 받아 안전하게 리팩토링을 수행하는 에이전트.
역할 정의: `agents/roles/refactor-agent.md`

---

## 에이전트 목표

1. 나쁜 코드 패턴 탐지 및 자동 수정 (BP-1~6)
2. 기술 부채 해소 (tech-debt-registry.md ACTIVE 항목)
3. 코드 커버리지 향상 (누락 테스트 추가)
4. 일관성 없는 구조 표준화

**핵심 원칙:** 리팩토링은 **동작을 변경하지 않는다**.
동작 변경이 필요하면 리팩토링이 아닌 기능 개선이며, 별도 Intent로 처리한다.

---

## 에이전트 상태 머신

```
IDLE
  │
  │ (assess-quality.sh 실행 → 품질 문제 발견)
  ▼
DETECTING
  │
  ├── 문제 없음 → IDLE
  │
  └── 문제 있음
       │
       ▼
PLANNING
  │ (plans/active/YYYY-MM-DD-refactor-<대상>.yaml 생성)
  ▼
EXPLORING
  │ (영향 범위 파악: git diff, 의존 클래스 목록)
  ▼
IMPLEMENTING
  │ (실제 코드 변경)
  │
  ├── 변경 범위가 작음 (< 5파일) → 계속
  └── 변경 범위가 큼 (≥ 5파일) → 인간 승인 요청 후 계속
  │
  ▼
VERIFYING
  │ (scripts/verify-gates.sh 실행)
  │
  ├── 모든 게이트 통과 → RECORDING
  └── 실패 (최대 3회 재시도)
       │
       ├── 3회 이내 → IMPLEMENTING (수정 후 재시도)
       └── 3회 초과 → ESCALATED
  │
  ▼
RECORDING
  │ (plans/completed/ 이동, enforcement-map 갱신)
  ▼
IDLE
```

---

## Intent 파일 형식

리팩토링 작업 시작 전 반드시 생성:

```yaml
# plans/active/YYYY-MM-DD-refactor-<대상명>.yaml
goal: "<리팩토링 목표 — 동작 변경 없음 명시>"
why: "코드 품질 향상: <bad-patterns.md BP-N> 해소"
constraints:
  - "모든 기존 테스트가 통과해야 한다"
  - "API 계약(api-spec.md)이 변경되지 않아야 한다"
  - "동작이 변경되면 즉시 중단하고 보고한다"
acceptance_criteria:
  - "CodeQualityTest.CQ-N 통과"
  - "기존 테스트 모두 통과"
  - "커버리지 기준 유지"
out_of_scope:
  - "기능 추가"
  - "새로운 도메인 규칙 추가"
  - "외부 의존성 변경"
```

---

## 자동 리팩토링 가능 범위

### ✅ 자동화 가능 (인간 승인 불필요)

| 대상 | 패턴 | 위험도 |
|---|---|---|
| 와일드카드 import 제거 | BP-? (STYLE-1 위반) | 🟢 LOW |
| 불필요한 import 제거 | STYLE-1 | 🟢 LOW |
| @Transactional 누락 추가 | TD-1 | 🟡 MEDIUM |
| 에러 메시지 상수화 | TD-3 | 🟡 MEDIUM |
| 메서드 추출 (Extract Method) | 긴 메서드 | 🟡 MEDIUM |
| 테스트 @DisplayName 추가 | Legibility | 🟢 LOW |

### ⚠️ 인간 승인 필요

| 대상 | 이유 |
|---|---|
| 클래스 분리 (God Service 해소) | 구조 변경, 영향 범위 큼 |
| 도메인 예외 계층 도입 | 새 클래스 추가, API 계약에 영향 가능 |
| 패키지 구조 변경 | ARCH 규칙 변경 가능성 |
| 외부 의존성 변경 | 인간 승인 정책 (Principle 1) |

---

## 안전 검증 프로토콜

리팩토링 후 반드시 이 순서로 검증:

```bash
# Step 1: 컴파일 확인
./gradlew compileJava compileTestJava

# Step 2: 기존 테스트 회귀 없음 확인
./gradlew test

# Step 3: 품질 게이트 통과 확인
./gradlew test --tests "*.CodeQualityTest"
./gradlew test --tests "*.ArchitectureTest"

# Step 4: 커버리지 기준 유지 확인
./gradlew jacocoTestCoverageVerification

# Step 5: 전체 빌드
./gradlew build
```

**모든 단계를 통과해야 리팩토링이 완료된 것이다.**

---

## 에스컬레이션 기준

다음 중 하나라도 해당하면 인간에게 즉시 보고:
- 같은 파일에서 동일 오류가 3회 이상 발생
- 리팩토링으로 인해 API 응답 형식이 변경됨
- 도메인 규칙 자체를 수정해야 하는 상황
- 외부 라이브러리 추가가 필요한 상황
