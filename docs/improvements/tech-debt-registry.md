---
title: "기술 부채 추적부"
type: project
domain: cross-cutting
load_level: 2
verified_at: "2026-04-16"
references_code: []
related:
  - system.md
  - bad-patterns.md
  - quality-metrics.md
  - ../../docs/constraints/enforcement-map.md
supersedes: null
superseded_by: null
---

# 기술 부채 추적부 (Technical Debt Registry)

현재 코드베이스에 존재하는 기술 부채 목록.
`scripts/assess-quality.sh`가 이 파일을 참조하여 부채 증감을 추적한다.

**원칙:** 부채는 발견 즉시 등록하고, 해소 시 상태를 갱신한다.
부채 없이 넘어가면 누적되어 나중에 더 큰 비용을 유발한다.

---

## 부채 상태 범례

| 상태 | 의미 |
|---|---|
| 🔴 ACTIVE | 현재 존재하는 부채 |
| 🟡 PLANNED | 해소 계획이 수립됨 |
| ✅ RESOLVED | 해소 완료 |

---

## 현재 기술 부채 목록

### TD-1: ProductService.create()에 @Transactional 누락

| 항목 | 내용 |
|---|---|
| **ID** | TD-1 |
| **상태** | 🔴 ACTIVE |
| **위치** | `ProductService.java:18` |
| **패턴** | BP-6 (Transactional 불일치) |
| **영향도** | LOW — 현재 단순 save() 호출이라 실질적 문제 없음 |
| **위험** | 미래에 복잡한 create 로직 추가 시 원자성 미보장 |
| **수정** | `@Transactional` 추가 |
| **탐지 수단** | ❌ MISSING (자동 탐지 없음) |

---

### TD-2: O-3 N+1 쿼리 자동 검증 미구현

| 항목 | 내용 |
|---|---|
| **ID** | TD-2 |
| **상태** | 🔴 ACTIVE |
| **위치** | `OrderRepository.findAllWithProduct()` — JOIN FETCH 있음 |
| **패턴** | 검증 부재 |
| **영향도** | MEDIUM — JOIN FETCH 제거 시 N+1 발생해도 테스트가 잡지 못함 |
| **위험** | 미래 리팩토링 중 JOIN FETCH 제거 → N+1 쿼리 → 성능 저하 |
| **수정** | 통합 테스트에서 SQL 쿼리 수 assertion 추가 (H2 + Hibernate Statistics) |
| **탐지 수단** | ⚠️ PARTIAL (enforcement-map O-3) |

---

### TD-3: 하드코딩 에러 메시지

| 항목 | 내용 |
|---|---|
| **ID** | TD-3 |
| **상태** | 🔴 ACTIVE |
| **위치** | `ProductService.java`, `OrderService.java` |
| **패턴** | BP-5 (하드코딩 에러 메시지) |
| **영향도** | LOW — 현재 문제 없음 |
| **위험** | 중복 문자열 오탈자, 테스트에서 문자열 하드코딩 의존 |
| **수정** | `ErrorMessages` 상수 클래스 추출 |
| **탐지 수단** | ⚠️ PARTIAL (assess-quality.sh grep) |

---

### TD-4: IFACE-2 HTTP 상태 코드 E2E 검증 미구현

| 항목 | 내용 |
|---|---|
| **ID** | TD-4 |
| **상태** | 🔴 ACTIVE |
| **위치** | 모든 Controller |
| **패턴** | 검증 부재 |
| **영향도** | MEDIUM — HTTP 상태 코드 계약이 검증되지 않음 |
| **위험** | API 계약 위반을 배포 후에 발견 |
| **수정** | `@SpringBootTest` + `MockMvc` E2E 테스트 작성 |
| **탐지 수단** | ❌ MISSING (enforcement-map IFACE-2) |

---

### TD-5: God Service 자동 탐지 미구현

| 항목 | 내용 |
|---|---|
| **ID** | TD-5 |
| **상태** | 🔴 ACTIVE |
| **위치** | `scripts/assess-quality.sh` |
| **패턴** | BP-2 (God Service 탐지 불가) |
| **영향도** | LOW — 현재 서비스 크기가 작아 문제 없음 |
| **위험** | 서비스가 커질수록 탐지 필요성 증가 |
| **수정** | assess-quality.sh에 의존성 수 분석 추가 |
| **탐지 수단** | ❌ MISSING |

---

## 해소된 부채

| ID | 내용 | 해소일 | 방법 |
|---|---|---|---|
| - | (해소 항목 없음) | - | - |

---

## 부채 등록 절차

1. 부채 발견 시 TD-N 형식으로 이 파일에 추가
2. `bad-patterns.md`에 해당 패턴이 없으면 BP-N으로 추가
3. `enforcement-map.md`에 탐지 수단 상태 반영
4. 해소 시 상태를 ✅ RESOLVED로 갱신하고 "해소된 부채" 섹션으로 이동
