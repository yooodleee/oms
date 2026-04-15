# Plans — 작업 계획 및 진행 상태

## 디렉토리 구조

```
plans/
├── active/      ← 현재 진행 중인 작업 intent
└── completed/   ← 완료된 작업 intent (결과 포함)
```

## 파일 명명

```
YYYY-MM-DD-<작업-키워드>.yaml
예: 2026-04-15-order-status-filter.yaml
```

## Intent 파일 형식

```yaml
# plans/active/YYYY-MM-DD-<작업명>.yaml

goal: ""
why: ""
constraints: []
acceptance_criteria: []
out_of_scope: []

# 작업 완료 후 아래 항목을 채워 plans/completed/로 이동
result:
  status: COMPLETED | FAILED | ESCALATED
  iterations: 0
  gates_passed: []
  failure_log: []
  harness_improved: false   # Principle 7: 하네스 개선 여부
```

## 에이전트 행동 기준

- 작업 시작 시 `active/`에 intent 파일 생성
- 작업 완료 시 `result` 섹션 채우고 `completed/`로 이동
- `harness_improved: false`인 채로 완료되면 Principle 7 위반