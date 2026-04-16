---
title: "코드 품질 측정 지표"
type: reference
domain: cross-cutting
load_level: 2
verified_at: "2026-04-16"
references_code:
  - scripts/assess-quality.sh
  - build.gradle
  - src/test/java/com/sparta/oms/quality/CodeQualityTest.java
related:
  - system.md
  - bad-patterns.md
supersedes: null
superseded_by: null
---

# 코드 품질 측정 지표

`scripts/assess-quality.sh`가 측정하고 `build.gradle`이 강제하는 지표 정의.

---

## 1. 테스트 커버리지 (JaCoCo)

**강제 수단:** `./gradlew jacocoTestCoverageVerification`
**리포트 위치:** `build/reports/jacoco/test/html/index.html`

| 패키지 | 측정 단위 | 최소 기준 | 상태 |
|---|---|---|---|
| `product.service` | LINE | 70% | ✅ ENFORCED |
| `order.service` | LINE | 70% | ✅ ENFORCED |
| `product.entity` | LINE | 80% | ✅ ENFORCED |
| `order.entity` | LINE | 80% | ✅ ENFORCED |

**커버리지 기준 해석:**
- 70%: 핵심 경로 + 주요 예외 케이스 커버
- 80%: 도메인 로직의 경계값까지 커버
- 100%: 요구하지 않음 (setter·getter 등 trivial 코드 포함 시 비효율)

---

## 2. 코드 품질 스멜 (ArchUnit)

**강제 수단:** `./gradlew test --tests "*.CodeQualityTest"`

| 규칙 ID | 내용 | 상태 |
|---|---|---|
| **CQ-1** | Entity 클래스 public setter 금지 | ✅ ENFORCED |
| **CQ-1** | Entity가 DTO 타입 반환 금지 | ✅ ENFORCED |
| **CQ-2** | DTO 클래스에 @Entity 금지 | ✅ ENFORCED |
| **CQ-2** | DTO가 Service에 의존 금지 | ✅ ENFORCED |
| **CQ-3** | 도메인 코드의 RuntimeException 직접 throw 금지 | ✅ ENFORCED |
| **CQ-4** | Repository는 repository 패키지에 위치 | ✅ ENFORCED |
| **CQ-4** | Service의 cross-domain Entity 직접 생성 금지 | ✅ ENFORCED |

---

## 3. 기술 부채 지표 (Shell 분석)

**측정 수단:** `scripts/assess-quality.sh`
**기록 위치:** `docs/improvements/tech-debt-registry.md`

| 지표 | 측정 방법 | 임계값 |
|---|---|---|
| TODO/FIXME 수 | `grep -rn "TODO\|FIXME\|HACK"` | 0개 (발견 시 즉시 처리) |
| 긴 메서드 | 메서드 당 줄 수 > 30 | 0개 |
| 하드코딩 문자열 메시지 | `grep -n "\".*not found\|\".*failed"` | 기록 후 상수화 권고 |
| enforcement-map MISSING | MISSING 항목 수 | 0개 (merge 차단) |

---

## 4. 품질 점수 산출

`scripts/assess-quality.sh` 실행 시 다음 기준으로 품질 점수를 출력한다.

```
품질 점수 = 100
  - (커버리지 미달 패키지 수 × 20)
  - (CodeQualityTest 실패 규칙 수 × 15)
  - (TODO/FIXME 수 × 5)
  - (enforcement-map MISSING 수 × 25)

점수 기준:
  90~100 : GOOD   — PR 즉시 가능
  70~89  : FAIR   — 기록 후 PR 가능, 다음 스프린트에 개선
  50~69  : POOR   — 개선 계획 수립 후 PR
  50 미만 : BLOCK  — 개선 후 재평가 필수
```

---

## 5. 지표 기준 변경 절차

지표 기준을 높이거나 낮추려면:
1. 이 파일(`quality-metrics.md`)을 수정한다
2. `build.gradle`의 `jacocoTestCoverageVerification` 임계값을 동기화한다
3. `enforcement-map.md`를 갱신한다
4. 변경 이유를 ADR 또는 `docs/improvements/`에 기록한다
