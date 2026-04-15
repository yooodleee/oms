---
title: "운영 모니터링 피드백 파이프라인"
type: architecture
domain: cross-cutting
load_level: 3
verified_at: "2026-04-15"
references_code:
  - src/main/resources/application.properties
related:
  - logging/strategy.md
  - metrics/strategy.md
  - ../docs/reliability/sla.md
  - ../docs/improvements/README.md
supersedes: null
superseded_by: null
---

# 운영 모니터링 피드백 파이프라인

테스트로 잡히지 않은 이상이 운영 환경에서 발생했을 때, 이를 감지·기록·하네스 개선으로 연결하는 파이프라인.

---

## 모니터링 신호 → 행동 매핑

### SQL 로그 신호 (현재 활성화)

```
신호: show-sql=true 에서 동일 테이블 SELECT 반복 감지

SELECT * FROM product WHERE id=1
SELECT * FROM product WHERE id=2   ← N+1 패턴
SELECT * FROM product WHERE id=3

행동:
  1. ADR-0003 참조 → JOIN FETCH 누락 여부 확인
  2. 해당 Repository 메서드 특정
  3. 즉각 수정 (JOIN FETCH 추가)
  4. docs/improvements/ 기록
  5. enforcement-map O-3 상태 재검토
```

### 애플리케이션 로그 신호

```
신호: IllegalArgumentException: "재고가 부족합니다." 빈도 급증

행동:
  - 단순 비즈니스 이상 (재고 부족 시도): 기록만
  - 재고가 이미 0인데 주문이 들어오는 경우: P-1 규칙 우회 의심 → 즉시 조사

신호: IllegalArgumentException: "Product not found" 빈도
  - 삭제된 상품을 대상으로 한 요청: P-2 정상 동작
  - 존재하는 상품인데 not found: 소프트 삭제 쿼리 버그 의심 → 즉시 조사
```

---

## 피드백 파이프라인 흐름

```
운영 이상 감지
      │
      ▼
 ┌──────────────────────────────────────────┐
 │  1. 즉각 분류                             │
 │     A. 도메인 규칙 위반 시도 (정상 거부)  │
 │     B. 시스템 버그 (조사 필요)            │
 │     C. 성능 이상 (N+1, 슬로우 쿼리)      │
 └─────────────────┬────────────────────────┘
                   │
      ┌────────────┼──────────────┐
      ▼            ▼              ▼
   [A] 기록만   [B] 즉각 수정   [C] 쿼리 최적화
                    │
                    ▼
             docs/improvements/
             YYYY-MM-DD-*.md 기록
                    │
                    ▼
             enforcement-map
             관련 규칙 상태 재검토
                    │
                    ▼
             테스트 추가
             (동일 이상이 테스트에서 잡히도록)
```

---

## 이상 등급별 대응 (reliability/sla.md 연동)

| 이상 유형 | 등급 | 감지 수단 | 대응 |
|---|---|---|---|
| 재고 음수 발생 | P0 | 로그 + DB 직접 확인 | 즉시 롤백 + 하네스 개선 |
| N+1 쿼리 발생 | P1 | SQL 로그 패턴 | 당일 JOIN FETCH 추가 |
| 삭제 상품 노출 | P0 | API 응답 확인 | 즉시 쿼리 수정 |
| 응답 시간 급증 | P1 | Actuator (미구성) | 슬로우 쿼리 분석 |
| 예외 빈도 급증 | P2 | 로그 | 다음 스프린트 조사 |

---

## 운영 피드백 → 하네스 개선 연결

```
운영에서 발견된 버그가 테스트에서 잡혔어야 한다면:

  질문: "어떤 테스트 레이어가 이것을 잡았어야 하는가?"
    └── 단위 테스트: 도메인 로직 버그
    └── 통합 테스트: 쿼리 버그 (deletedAt IS NULL 누락 등)
    └── E2E 테스트: API 계약 위반

  행동:
    → 해당 레이어에 새 테스트 추가
    → enforcement-map ❌ MISSING → ✅ ENFORCED
    → docs/improvements/ 기록 (탈출 경로 분석 포함)

결과: 같은 버그는 다음 배포 전 반드시 테스트에서 잡힌다
```

---

## 피드백 수집 주기

| 수집 대상 | 주기 | 저장 위치 |
|---|---|---|
| 단위·통합·E2E 테스트 | 커밋마다 (CI) | `build/reports/` (ephemeral) |
| SQL 로그 분석 | 배포 후 관찰 기간 중 | `docs/improvements/` (필요시) |
| enforcement-map 갱신 | 기능 구현 완료마다 | `docs/constraints/enforcement-map.md` |
| 개선 기록 | 버그 발생마다 | `docs/improvements/YYYY-MM-DD-*.md` |
| ADR verified_at 갱신 | 통합 테스트 통과마다 | 해당 ADR frontmatter |
