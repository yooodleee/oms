# Agents — 에이전트 역할 및 워크플로우

이 디렉토리는 하네스 엔지니어링 시스템에서 각 에이전트의 **책임, 입력, 출력, 행동 제약**을 정의한다.

## 에이전트 풀

| 에이전트 | 역할 | 정의 파일 |
|---|---|---|
| Plan | 구현 전략 수립, 트레이드오프 분석 | [roles/plan-agent.md](roles/plan-agent.md) |
| Explore | 코드베이스 탐색, 영향 범위 파악 | [roles/explore-agent.md](roles/explore-agent.md) |
| Implement | 실제 코드 작성 | [roles/implement-agent.md](roles/implement-agent.md) |
| Test | 테스트 작성 및 실행 | [roles/test-agent.md](roles/test-agent.md) |
| Review | 변경 검증, 원칙 준수 확인 | [roles/review-agent.md](roles/review-agent.md) |
| Report | 결과 보고, 문서 갱신 | [roles/report-agent.md](roles/report-agent.md) |

## 에이전트 운영 시스템

전체 AOS 설계 (상태 머신, 수신~완료 전체): [operating-system.md](operating-system.md)

## 워크플로우

| 작업 유형 | 워크플로우 |
|---|---|
| 신규 기능 구현 | [workflows/feature-workflow.md](workflows/feature-workflow.md) |
| PR 생성·관리 | [workflows/pr-workflow.md](workflows/pr-workflow.md) |
| 에이전트 리뷰 루프 | [workflows/review-loop.md](workflows/review-loop.md) |
| 실패 복구 전략 | [workflows/retry-strategy.md](workflows/retry-strategy.md) |

## 에이전트 공통 원칙

1. **탐색 우선**: 코드를 수정하기 전에 반드시 현재 상태를 탐색한다 (Principle 2)
2. **단일 책임**: 각 에이전트는 자신의 역할만 수행한다. 구현 에이전트가 테스트를 작성하지 않는다
3. **레포 내 컨텍스트**: 외부 기억이나 대화 기록에 의존하지 않는다 (Principle 3)
4. **게이트 통과 필수**: 모든 에이전트 출력은 검증 레이어를 통과해야 한다 (Principle 4·6)