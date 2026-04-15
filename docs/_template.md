---
# 문서 메타데이터 표준 (모든 docs/ 파일에 이 frontmatter를 포함해야 한다)

title: ""                          # 문서 제목
type: ""                           # adr | constraint | context | architecture
                                   # design | agent-role | workflow | improvement
                                   # product | security | reliability | index
domain: ""                         # product | order | cross-cutting | harness
load_level: 0                      # 점진적 로딩 레벨 (아래 정의 참조)
verified_at: "YYYY-MM-DD"          # 마지막으로 코드와 대조하여 검증한 날짜
references_code:                   # 이 문서가 참조하는 실제 코드 경로
  - ""                             # validate-docs.sh가 존재 여부를 검증
related:                           # 연관 문서 (읽어야 할 다음 문서)
  - ""
supersedes: null                   # 이 문서가 대체하는 문서
superseded_by: null                # 이 문서를 대체한 문서 (비워두면 유효)
---

# [문서 제목]

## [섹션]

...

## 에이전트 주의사항

이 문서를 읽는 에이전트가 반드시 알아야 할 행동 제약.
```

---

## 점진적 로딩 레벨 정의

| 레벨 | 로딩 시점 | 해당 문서 유형 |
|---|---|---|
| **L0** | 항상 로딩 | CLAUDE.md, docs/index.md |
| **L1** | 작업 시작 시 | architecture/overview.md, 도메인 CONTEXT.md |
| **L2** | 구현 전 | domain-rules.md, enforcement-map.md, 관련 ADR |
| **L3** | 심층 분석 시 | 특정 ADR 전문, improvements/ 기록, 보안·신뢰성 정책 |

에이전트는 L0 → L1 → L2 → L3 순서로 필요한 레벨까지만 로딩한다.
L3까지 로딩해야 하는 상황은 예외적이어야 한다.

## 문서 유형별 책임 (단일 책임 원칙)

| 유형 | 책임 | 책임 밖 |
|---|---|---|
| `adr` | 결정의 근거·결과 | 구현 방법 |
| `constraint` | 위반 불가 규칙 목록 | 규칙의 근거 (→ adr로) |
| `context` | 도메인 탐색 진입점 | 전체 아키텍처 (→ architecture로) |
| `architecture` | 시스템 전체 구조 | 도메인 세부 (→ context로) |
| `improvement` | 특정 버그/개선 이력 | 전체 규칙 (→ constraint로) |
