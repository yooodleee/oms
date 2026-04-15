# Agent Operating System (AOS)

에이전트가 작업을 수신·수행·검증·실패 복구하는 완전한 운영 체계.

---

## 1. 상태 머신 (State Machine)

```
                         ┌─────────────────────────────────────────────────────┐
                         │                AGENT OPERATING SYSTEM                │
                         └─────────────────────────────────────────────────────┘

  [인간]
  자연어 요청 또는
  intent 파일 생성
       │
       ▼
  ┌─────────┐
  │RECEIVING│  작업 수신
  └────┬────┘
       │ intent 파일 유효성 확인
       │ (goal, constraints, acceptance_criteria 필수)
       ▼
  ┌─────────┐
  │PLANNING │  Plan Agent: 구현 전략 수립
  └────┬────┘
       │ 전략 확정
       ▼
  ┌──────────┐
  │EXPLORING │  Explore Agent: 현재 코드 상태 탐색
  └────┬─────┘
       │ 영향 범위, 베이스라인 품질 파악
       ▼
  ┌────────────┐
  │IMPLEMENTING│  Implement Agent: 코드 작성
  └──────┬─────┘
         │
    [L1 Gate]─── FAIL ──▶ RETRYING(L1) ──▶ 3회 초과 ──▶ ESCALATED
         │ PASS
         ▼
  ┌─────────┐
  │ TESTING │  Test Agent: 테스트 작성 및 실행
  └────┬────┘
       │
  [L2 Gate]─── FAIL ──▶ RETRYING(L2) ──▶ 3회 초과 ──▶ ESCALATED
       │ PASS
       ▼
  ┌──────────┐
  │REVIEWING │  Review Agent: 7원칙 체크리스트
  └────┬─────┘
       │
  [Review]─── FAIL ──▶ RETRYING(Review) ──▶ 2회 초과 ──▶ ESCALATED
       │ PASS
       ▼
  ┌──────────┐
  │PR_READY  │  PR 생성 (pr-workflow.md)
  └────┬─────┘
       │ PR 병합
       ▼
  ┌────────┐
  │COMPLETE│  Report Agent: 결과 기록, Slack 보고
  └────────┘
```

---

## 2. 작업 수신 (Task Reception)

### 입력 계약: Intent 파일

모든 작업은 `plans/active/`의 intent 파일로 시작한다. 파일이 없으면 작업을 시작하지 않는다.

```yaml
# plans/active/YYYY-MM-DD-<keyword>.yaml

goal: ""                    # 필수: 달성해야 할 것
why: ""                     # 필수: 비즈니스 맥락
constraints: []             # 필수: 깨면 안 되는 조건
acceptance_criteria: []     # 필수: 완료 판정 기준
out_of_scope: []            # 필수: 이번 범위 밖

# 에이전트가 작업 중 채우는 항목
_agent_meta:
  state: RECEIVING          # 현재 상태
  plan_summary: ""          # Plan Agent가 작성
  branch: ""                # Implement Agent가 작성
  pr_url: ""                # Report Agent가 작성
  started_at: ""
```

### 수신 유효성 검사

Plan Agent가 수신 시 아래를 확인한다. 하나라도 미충족이면 인간에게 반환:

- [ ] `goal`이 단일 목표를 명확히 표현하는가?
- [ ] `acceptance_criteria`가 기계적으로 검증 가능한가?
- [ ] `constraints`가 기존 `domain-rules.md`와 충돌하지 않는가?
- [ ] `out_of_scope`가 명시됐는가?

---

## 3. 컨텍스트 수집 (Context Collection)

Explore Agent가 **탐색 우선 프로토콜** (Principle 2)을 실행한다.

```
수집 순서                     수집 내용                       용도
─────────────────────────────────────────────────────────────────
1. git status, git log    현재 브랜치, 미커밋 변경         충돌 방지
2. Glob("**/*.java")      코드 구조 전체                   영향 범위 파악
3. ./gradlew test         현재 테스트 통과 현황             베이스라인 설정
4. enforcement-map.md     관련 규칙 강제 상태               MISSING 항목 파악
5. 도메인 CONTEXT.md      도메인별 주의사항                 구현 제약 확인
6. 관련 ADR               과거 결정 근거                   트레이드오프 참조
```

### 컨텍스트 수집 결과물 (Plan Agent로 전달)

```yaml
context_snapshot:
  baseline_tests_pass: true | false
  affected_files: []
  related_rules: []          # 관련 domain-rules ID
  missing_enforcements: []   # enforcement-map MISSING 항목
  adr_references: []         # 관련 ADR 번호
  risk_areas: []             # 주의가 필요한 영역
```

---

## 4. 작업 수행 (Task Execution)

### 에이전트 파이프라인

```
Plan Agent
  입력: intent + context_snapshot
  출력: implementation_plan (변경할 파일, 추가할 메서드, 전략)
    ↓
Implement Agent
  입력: implementation_plan + 도메인 CONTEXT.md
  출력: 변경된 소스 파일 + [L1 Gate 통과]
    ↓
Test Agent
  입력: acceptance_criteria + 변경된 소스 파일
  출력: 테스트 코드 + [L2 Gate 통과] + enforcement-map 갱신
    ↓
Review Agent
  입력: 전체 diff + intent + enforcement-map
  출력: PASS 또는 FAIL + 피드백
```

### 작업 수행 불변 규칙

- 에이전트는 자신의 역할 밖의 행동을 하지 않는다
- `out_of_scope` 항목에 해당하는 파일은 수정하지 않는다
- 외부 라이브러리 추가 → 즉시 ESCALATED
- `domain-rules.md` 규칙 자체 변경 → 즉시 ESCALATED

---

## 5. 결과 검증 (Result Verification)

`scripts/verify-gates.sh` 또는 수동 실행.

```
Gate    명령                      통과 기준               실패 시
──────────────────────────────────────────────────────────────────
L1      ./gradlew compileJava     exit code 0             RETRYING(L1)
L2      ./gradlew test            모든 테스트 통과          RETRYING(L2)
L3      ./gradlew build           아티팩트 생성 성공        RETRYING(L3)
L4      HTTP 응답 확인 (선택)      acceptance 기준 충족      RETRYING(L4)
L5      SQL 로그 확인 (선택)       N+1 없음                 RETRYING(L5)
```

---

## 6. 실패 시 재시도 (Retry on Failure)

`agents/workflows/retry-strategy.md` 참조.

**재시도 한도 요약:**

| 게이트 | 최대 재시도 | 초과 시 |
|---|---|---|
| L1 컴파일 | 3회 | ESCALATED |
| L2 테스트 | 3회 | ESCALATED |
| Review | 2회 | ESCALATED |

---

## 7. 상태 전이 불변 조건

- `COMPLETE` 상태에 도달하려면 반드시 L1, L2, L3를 통과해야 한다
- `ESCALATED` 상태는 인간의 명시적 확인 없이 다른 상태로 전이할 수 없다
- `PR_READY` 상태에서 게이트를 재실행했을 때 실패하면 `IMPLEMENTING`으로 되돌아간다