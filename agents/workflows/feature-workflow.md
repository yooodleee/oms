# 신규 기능 구현 워크플로우

## 트리거
인간이 `plans/active/`에 intent 파일을 생성하거나 자연어로 기능 요청을 한다.

## 실행 순서

```
Step 1: Intent 구조화                          [인간 또는 Plan Agent]
  plans/active/YYYY-MM-DD-<작업명>.yaml 생성
  goal / why / constraints / acceptance_criteria / out_of_scope 작성

Step 2: 탐색                                   [Explore Agent]
  git status, git log
  Glob("**/*.java") → 코드 구조 파악
  ./gradlew test → 현재 품질 베이스라인
  enforcement-map 관련 규칙 상태 확인

Step 3: 계획 수립                               [Plan Agent]
  Explore Agent 결과를 바탕으로 구현 전략 수립
  enforcement-map MISSING 항목 파악
  트레이드오프 분석

Step 4: 구현                                   [Implement Agent]
  Plan Agent 전략에 따라 코드 작성
  파일 수정 후 L1(compileJava) 즉시 확인
  enforcement-map MISSING 항목 강제 수단 추가

Step 5: 테스트                                 [Test Agent]
  acceptance_criteria → 테스트 케이스 도출
  ./gradlew test 전체 통과
  enforcement-map 상태 갱신 (❌ → ✅)

Step 6: 검토                                   [Review Agent]
  7개 원칙 체크리스트 전항목 통과 확인

Step 7: 보고                                   [Report Agent]
  plans/active/ → plans/completed/ 이동
  enforcement-map 최종 갱신
  Slack 보고
```

## 에스컬레이션 조건 (인간 개입 필요)
- acceptance_criteria 달성 불가 (3회 재시도 소진)
- constraints 간 충돌 발생
- 외부 라이브러리 추가 필요
- 도메인 규칙(`domain-rules.md`) 자체의 변경이 필요한 경우