# PR 생성 및 관리 흐름

PR은 Review Agent가 PASS를 승인한 이후에만 생성된다. 게이트를 통과하지 않은 코드는 PR을 열지 않는다.

---

## PR 생성 조건

```
전제 조건 (모두 충족해야 PR 생성 가능):
  ✅ L1 (compileJava) PASS
  ✅ L2 (test) PASS — ArchUnit 포함
  ✅ L3 (build) PASS
  ✅ Review Agent 체크리스트 PASS
  ✅ enforcement-map MISSING 항목 없음 (관련 항목 한정)
```

---

## 브랜치 명명 규칙

```
<유형>/<YYYY-MM-DD>-<intent-keyword>

유형:
  feat/   신규 기능
  fix/    버그 수정 (+ 하네스 개선 포함)
  refactor/ 리팩토링
  harness/  하네스 시스템 자체 변경

예:
  feat/2026-04-15-order-status-filter
  fix/2026-04-15-soft-delete-query-missing-filter
  harness/2026-04-15-add-archunit-rules
```

---

## PR 본문 자동 생성 템플릿

intent 파일에서 자동으로 PR 본문을 구성한다.

```markdown
## Intent

**Goal:** <intent.goal>
**Why:** <intent.why>

## 변경 내용

- 변경 파일 목록 (git diff --name-only)

## 검증 게이트 통과

- [x] L1: 컴파일
- [x] L2: 단위 테스트 + ArchUnit
- [x] L3: 빌드
- [x] Review Agent: 7원칙 체크리스트

## Enforcement Map 변경

| 규칙 ID | 이전 상태 | 이후 상태 |
|---|---|---|
| <규칙ID> | ❌ MISSING | ✅ ENFORCED |

## 관련 문서

- Intent: `plans/active/YYYY-MM-DD-<keyword>.yaml`
- ADR: `docs/adr/NNNN-<title>.md` (있을 경우)

## Out of Scope (변경하지 않은 것)

<intent.out_of_scope 목록>
```

---

## PR 라이프사이클

```
브랜치 생성
    │  git checkout -b feat/YYYY-MM-DD-<keyword>
    ▼
코드 변경 + 게이트 통과
    │  scripts/verify-gates.sh
    ▼
PR 생성 (Draft → Ready)
    │  게이트 통과 전: Draft PR
    │  게이트 통과 후: Ready for Review
    ▼
에이전트 리뷰 루프
    │  review-loop.md 참조
    ▼
병합 조건 최종 확인
    │  - 모든 게이트 PASS
    │  - Review Agent PASS
    │  - enforcement-map 갱신 완료
    ▼
병합 (Squash Merge)
    │  커밋 메시지: "<유형>(<도메인>): <intent.goal 요약>"
    ▼
intent 파일 이동
    │  plans/active/ → plans/completed/
    ▼
브랜치 삭제
```

---

## PR 메시지 커밋 형식

```
<유형>(<도메인>): <목표 요약>

- <변경 핵심 1>
- <변경 핵심 2>

Gates: L1✅ L2✅ L3✅ Review✅
Enforcement: <규칙ID> MISSING→ENFORCED

Intent: plans/completed/YYYY-MM-DD-<keyword>.yaml
```

---

## PR 차단 조건 (자동 거부)

Review Agent 또는 CI 게이트가 아래 항목을 감지하면 PR을 병합하지 않는다:

| 조건 | 근거 |
|---|---|
| `./gradlew test` 실패 | Principle 6 — 강제 제약 |
| enforcement-map에 새로운 `❌ MISSING` 발생 | Principle 6 |
| out_of_scope 파일이 diff에 포함 | Principle 1 — intent 계약 위반 |
| `domain-rules.md` 규칙 변경 (인간 승인 없이) | Principle 1 |
| 스택 트레이스가 API 응답에 포함 | `docs/security/security-policy.md` |