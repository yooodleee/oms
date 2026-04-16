---
title: "지속적 개선 시스템"
type: architecture
domain: cross-cutting
load_level: 1
verified_at: "2026-04-16"
references_code:
  - scripts/assess-quality.sh
  - src/test/java/com/sparta/oms/quality/CodeQualityTest.java
  - build.gradle
related:
  - quality-metrics.md
  - bad-patterns.md
  - tech-debt-registry.md
  - refactoring-agent.md
  - ../../agents/roles/refactor-agent.md
  - ../../docs/constraints/enforcement-map.md
supersedes: null
superseded_by: null
---

# 지속적 개선 시스템

에이전트가 생성한 코드의 품질을 지속적으로 측정하고, 나쁜 패턴을 탐지하며,
기술 부채를 추적하고, 자동으로 개선하는 시스템.

---

## 시스템 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                  지속적 개선 시스템                               │
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │  품질 평가   │───▶│  탐지 결과   │───▶│ 자동 리팩토링   │   │
│  │ Evaluation   │    │  Registry    │    │  Agent           │   │
│  └──────────────┘    └──────────────┘    └──────────────────┘   │
│         │                   │                      │             │
│         ▼                   ▼                      ▼             │
│  scripts/             tech-debt-            plans/active/        │
│  assess-quality.sh    registry.md           intent 파일         │
│                                                                  │
│  측정 도구:                                                       │
│  - JaCoCo (커버리지)    build/reports/jacoco/                    │
│  - ArchUnit (품질 스멜) CodeQualityTest.java                     │
│  - Checkstyle (스타일)  build/reports/checkstyle/               │
│  - Shell 분석 (구조)    assess-quality.sh                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 개선 트리거 3종

### Trigger 1: 정기 평가 (주기적 탐지)
```
scripts/assess-quality.sh 실행
    │
    ├── 커버리지 < 임계값 → tech-debt-registry.md 갱신
    ├── 품질 스멜 탐지 → bad-patterns.md 참조 후 수정
    ├── TODO/FIXME 증가 → 부채 항목 추가
    └── 모든 지표 정상 → 기록 없음
```

### Trigger 2: 게이트 실패 (반응적 탐지)
```
./gradlew build 실패
    │
    ├── L1 (컴파일) 실패 → 즉각 수정
    ├── L2 (CodeQualityTest) 실패 → bad-patterns.md 참조 → 수정
    ├── L3 (JaCoCo 커버리지) 실패 → 누락 테스트 추가
    └── 동일 실패 2회 이상 → docs/improvements/ 에 패턴 기록
```

### Trigger 3: 버그 탈출 (사후 분석)
```
production 버그 발견
    │
    ├── 어떤 게이트가 이것을 놓쳤는가?
    ├── 해당 게이트에 케이스 추가
    ├── enforcement-map.md 갱신
    └── docs/improvements/YYYY-MM-DD-<이슈명>.md 기록
```

---

## 개선 루프 (Principle 7 구현)

```
문제 발생
    │
    ▼
┌─────────────────────┐
│  1. 즉각 수정        │  ← 코드 변경
│     (증상 치료)      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  2. 근본 원인 분석   │  ← 어떤 레이어가 놓쳤는가?
│     Root Cause      │    L1? L2? L3? 가드레일?
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  3. 하네스 개선      │  ← 테스트 추가 or ArchUnit 규칙 추가
│     Harness Fix     │    or Checkstyle 규칙 추가
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  4. 개선 기록        │  ← docs/improvements/YYYY-MM-DD-*.md
│     Record          │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  5. 상태 갱신        │  ← enforcement-map.md 업데이트
│     Update Map      │
└─────────────────────┘
```

---

## 자동화 게이트 실행 순서

```bash
# 1단계: 코드 스타일
./gradlew checkstyleMain

# 2단계: 품질 스멜 + 아키텍처
./gradlew test --tests "*.CodeQualityTest"
./gradlew test --tests "*.ArchitectureTest"

# 3단계: 도메인 규칙
./gradlew test --tests "*.entity.*" --tests "*.service.*"

# 4단계: 커버리지
./gradlew jacocoTestCoverageVerification

# 전체 일괄 실행
scripts/assess-quality.sh
```

---

## 완료 기준 (Principle 7)

- 코드 수정만 있고 하네스 개선이 없으면 **작업 미완료**
- 동일 유형의 문제가 2회 이상 발생하면 구조적 차단 수단 추가 필수
- 새 도메인 규칙 추가 시 enforcement-map.md에 즉시 반영
