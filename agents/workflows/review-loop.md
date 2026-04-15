# 에이전트 간 리뷰 루프

구현과 테스트가 완료된 후 PR 생성 전에 실행되는 품질 검증 루프.

---

## 리뷰 루프 구조

```
                    ┌──────────────────────────────────────────────┐
                    │              REVIEW LOOP                      │
                    │                                              │
  [Test Agent]      │   Review Agent              피드백 대상      │
  L2 PASS           │       │                                      │
      │             │       ▼                                      │
      └────────────▶│  원칙 1~7 체크리스트                          │
                    │       │                                      │
                    │  PASS─┼─────────────────▶ PR_READY           │
                    │       │                                      │
                    │  FAIL─┼──▶ 피드백 분류                        │
                    │       │        │                             │
                    │       │   구현 문제 ──▶ Implement Agent       │
                    │       │   테스트 문제 ──▶ Test Agent          │
                    │       │   문서 문제 ──▶ Report Agent          │
                    │       │                    │                 │
                    │       │             수정 완료                 │
                    │       │                │                     │
                    │       │       L1, L2 재통과 확인              │
                    │       │                │                     │
                    │       └◀───────────────┘ (최대 2회)          │
                    │                                              │
                    │  2회 초과 FAIL ──▶ ESCALATED                 │
                    └──────────────────────────────────────────────┘
```

---

## Review Agent 체크리스트 (7원칙 대응)

### [P1] Agent-Centric
```
□ 구현이 intent의 acceptance_criteria를 모두 충족하는가?
□ out_of_scope 파일이 diff에 포함되지 않았는가?
□ acceptance_criteria 자체가 변경되지 않았는가?
```

### [P2] Environment-Centric
```
□ 탐색 우선 프로토콜이 실행됐는가? (context_snapshot 존재 확인)
□ 코드 변경이 현재 환경 상태를 반영하는가?
```

### [P3] In-Repository Knowledge
```
□ 새 아키텍처 결정이 있다면 ADR이 작성됐는가?
□ 도메인 규칙 변경이 domain-rules.md에 반영됐는가?
□ 관련 CONTEXT.md에 새 주의사항이 추가됐는가?
```

### [P4] Feedback Loop
```
□ intent 파일의 result 섹션이 채워졌는가?
□ 거쳤던 재시도 횟수가 기록됐는가?
□ gates_passed 목록이 정확한가?
```

### [P5] Legibility
```
□ 새 메서드명이 의도를 완전히 표현하는가?
□ 테스트 명명이 [규칙ID] 형식을 따르는가?
□ 새 쿼리에 ADR 또는 도메인 규칙 @see 참조가 있는가?
```

### [P6] Enforced Constraints
```
□ 관련 enforcement-map 항목이 ✅ ENFORCED인가?
□ 새로 ❌ MISSING 항목이 생기지 않았는가?
□ ArchUnit 테스트가 여전히 통과하는가?
```

### [P7] Continuous Improvement
```
□ 버그 수정이라면 docs/improvements/에 기록됐는가?
□ harness_improved 항목이 true인가?
```

---

## 리뷰 실패 피드백 형식

Review Agent가 실패를 반환할 때 반드시 아래 형식으로 구체적 피드백을 제공한다.

```yaml
review_feedback:
  cycle: 1  # 현재 리뷰 사이클 번호 (최대 2)
  failed_checks:
    - principle: P5
      check: "테스트 명명이 [규칙ID] 형식을 따르는가?"
      finding: "OrderServiceTest.testCreateOrder → [O-1] 형식 미준수"
      target_agent: Test Agent
      required_action: "메서드명을 [O-1] 형식으로 변경"

    - principle: P6
      check: "관련 enforcement-map 항목이 ✅ ENFORCED인가?"
      finding: "O-1 항목이 여전히 ❌ MISSING"
      target_agent: Test Agent
      required_action: "O-1 강제 테스트 추가 후 enforcement-map 갱신"
```

---

## 리뷰 사이클 상한

| 사이클 | 동작 |
|---|---|
| 1회차 FAIL | 피드백 → 해당 에이전트 수정 → L1, L2 재통과 → 2회차 리뷰 |
| 2회차 FAIL | ESCALATED — 인간에게 피드백 내용과 함께 보고 |
| 2회차 PASS | PR_READY |

**이유:** 2회 이상 리뷰가 실패하면 intent 자체의 모호성이나 constraints 충돌이 원인일 가능성이 높다. 에이전트가 혼자 해결하려 시도할수록 더 복잡해진다.

---

## 리뷰 루프와 PR 관계

```
Review Loop 1회차 PASS → PR Draft 생성
Review Loop 최종 PASS  → PR Ready for Review 전환
병합 직전              → scripts/verify-gates.sh 재실행 (최종 확인)
```