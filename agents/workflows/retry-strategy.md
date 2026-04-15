# 실패 복구 전략 (Failure Recovery Strategy)

에이전트가 게이트 실패 시 어떻게 복구하는지 정의한다.
복구는 기계적이어야 한다: 원인 → 분류 → 대응 → 재검증.

---

## 복구 원칙

```
1. 에러 메시지를 완전히 읽는다   ← 증상이 아닌 원인을 찾는다
2. 실패 유형을 분류한다          ← 분류에 따라 대응이 달라진다
3. 최소한의 변경만 한다          ← 복구 시도가 새 문제를 만들지 않도록
4. 변경 후 L1부터 다시 확인한다  ← 복구가 새 실패를 유발하지 않도록
5. 동일 에러가 2회 연속이면      ← 접근 방식을 바꾼다 (같은 시도 반복 금지)
```

---

## L1 (컴파일) 실패 복구

```
실패 감지: ./gradlew compileJava exit code ≠ 0

분류 및 대응:

  유형 A: 심볼 없음 (cannot find symbol)
    원인: import 누락 또는 클래스명 오타
    대응: Grep으로 실제 클래스명·패키지 확인 → import 수정
    재확인: ./gradlew compileJava

  유형 B: 타입 불일치 (incompatible types)
    원인: 메서드 반환 타입 또는 파라미터 타입 오류
    대응: 해당 메서드 시그니처 Read → 타입 맞춤
    재확인: ./gradlew compileJava

  유형 C: 메서드 없음 (cannot find method)
    원인: 아직 구현하지 않은 메서드 호출
    대응: Plan Agent의 구현 계획 재확인 → 누락 구현 추가
    재확인: ./gradlew compileJava

한도: 3회 재시도
3회 초과: ESCALATED — 컴파일 에러 전문과 함께 인간에게 보고
```

---

## L2 (테스트) 실패 복구

```
실패 감지: ./gradlew test 내 실패 테스트 존재

Step 1: 실패 테스트 격리
  ./gradlew test --tests "*.FailingTest"

Step 2: 실패 유형 분류

  ┌─ 회귀(Regression) ─────────────────────────────────────┐
  │ 정의: 기존에 통과하던 테스트가 이번 변경으로 실패         │
  │ 확인: git stash → ./gradlew test → stash pop           │
  │       stash 후 통과하면 = 회귀                          │
  │ 대응: 구현 코드 수정 (테스트는 수정하지 않는다)           │
  └────────────────────────────────────────────────────────┘

  ┌─ 의도된 변경(Intentional) ─────────────────────────────┐
  │ 정의: acceptance_criteria 변경으로 기존 동작이 바뀐 경우  │
  │ 확인: intent의 acceptance_criteria와 비교               │
  │ 대응: 테스트를 새 동작에 맞게 수정                       │
  │ 주의: 테스트 삭제 금지 — 반드시 새 케이스로 대체          │
  └────────────────────────────────────────────────────────┘

  ┌─ 환경 문제(Environment) ───────────────────────────────┐
  │ 정의: 테스트 자체는 맞지만 환경 설정 문제로 실패          │
  │ 확인: DB 연결, 환경 변수 확인                           │
  │ 대응: 환경 설정 수정 (코드 수정 아님)                    │
  └────────────────────────────────────────────────────────┘

Step 3: 대응 후 재실행
  ./gradlew test (전체 — 부분 실행으로 숨겨진 실패 방지)

한도: 3회 재시도
3회 초과: ESCALATED — 실패 테스트명, 에러, 시도한 수정 기록과 함께 보고
```

---

## Review 실패 복구

```
실패 감지: Review Agent 체크리스트 항목 미통과

대응 (피드백의 target_agent에 따라):

  target: Implement Agent
    → 해당 소스 파일 수정
    → L1 재확인 → L2 재확인 → Review 재실행

  target: Test Agent
    → 테스트 코드 수정 (명명, 누락 케이스, enforcement-map 갱신)
    → L2 재확인 → Review 재실행

  target: Report Agent
    → 문서(ADR, CONTEXT.md, enforcement-map) 갱신
    → Review 재실행 (코드 재확인 불필요)

한도: 2회 리뷰 사이클
2회 초과: ESCALATED — review_feedback 전체와 함께 인간에게 보고
```

---

## 반복 실패 패턴 (Recurring Failure)

같은 유형의 실패가 2회 이상 발생하면 접근 방식 자체를 바꾼다.

```
감지 조건: 동일한 에러 메시지 또는 동일한 체크 항목이 2회 연속 실패

대응 절차:
  1. 시도한 수정 내역을 모두 나열한다
  2. 각 수정이 실패한 이유를 분석한다
  3. 근본 원인이 intent의 모호성인가? → ESCALATED (intent 재정의 요청)
  4. 근본 원인이 constraints 충돌인가? → ESCALATED (충돌 내용 명시)
  5. 근본 원인이 구현 지식 부족인가? → 다른 구현 전략 시도 (Plan Agent 재실행)

반복 실패 기록:
  docs/improvements/에 패턴 기록 (Principle 7)
  → 동일 패턴 재발 방지를 위한 하네스 개선 수반
```

---

## ESCALATED 상태 보고 형식

```yaml
escalation_report:
  intent_file: "plans/active/YYYY-MM-DD-<keyword>.yaml"
  failed_gate: "L2 | Review | L1"
  retry_count: 3
  error_summary: "<에러 메시지 요약>"
  attempted_fixes:
    - attempt: 1
      change: "<수정 내용>"
      result: "<결과>"
    - attempt: 2
      change: "<수정 내용>"
      result: "<결과>"
  root_cause_hypothesis: "<원인 가설>"
  human_decision_needed: "<인간에게 필요한 결정>"
```

인간이 결정을 내리면 에이전트는 `RECEIVING` 상태로 되돌아가 재시작한다.