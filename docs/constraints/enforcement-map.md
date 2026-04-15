# 제약 조건 강제 매핑 (Enforcement Map)

**원칙:** 이 파일에 등록되지 않은 규칙은 "권장 사항"으로 간주되어 무효다.
모든 규칙은 반드시 기계적으로 강제되는 수단을 가져야 한다.

---

## 강제 상태 범례

| 상태 | 의미 |
|---|---|
| ✅ ENFORCED | 자동 검증 수단이 존재하고 CI에서 실행됨 |
| ⚠️ PARTIAL | 일부 케이스만 강제됨, 보완 필요 |
| ❌ MISSING | 강제 수단 없음 — 이 상태로 merge 금지 |

---

## 도메인 규칙 강제 현황

| 규칙 ID | 내용 | 강제 수단 | 테스트 위치 | 상태 |
|---|---|---|---|---|
| **P-1** | 재고는 음수가 될 수 없다 | JUnit | `ProductTest.decreaseStock_재고_부족_예외발생` | ✅ ENFORCED |
| **P-2** | 삭제된 상품은 조회 대상에서 제외 | JUnit | (추가 필요) | ❌ MISSING |
| **P-3** | 상품 가격·재고는 0 이상 | JUnit | (추가 필요) | ❌ MISSING |
| **O-1** | 주문 생성과 재고 차감은 원자적 | JUnit | (추가 필요) | ❌ MISSING |
| **O-2** | 주문은 삭제하지 않는다 | (삭제 API 미존재) | DELETE /orders endpoint 없음 | ⚠️ PARTIAL |
| **O-3** | 주문 목록 조회는 N+1 쿼리 없음 | SQL count assertion | (추가 필요) | ❌ MISSING |

---

## 아키텍처 규칙 강제 현황

| 규칙 ID | 내용 | 강제 수단 | 테스트 위치 | 상태 |
|---|---|---|---|---|
| **ARCH-1** | Controller → Repository 직접 접근 금지 | ArchUnit | `ArchitectureTest` | ✅ ENFORCED |
| **ARCH-2** | 레이어 의존성 방향 강제 (controller→service→repository) | ArchUnit | `ArchitectureTest` | ✅ ENFORCED |
| **ARCH-3** | 도메인 예외는 기반 예외 클래스 상속 | ArchUnit | (추가 필요) | ❌ MISSING |

---

## MISSING 항목 해소 기준

`❌ MISSING` 상태의 규칙은 다음 중 하나를 선택해야 한다:

1. **강제 수단 추가** → 테스트 작성 후 이 파일의 상태를 `✅ ENFORCED`로 갱신
2. **규칙 삭제** → 강제할 수 없거나 불필요하다고 판단된 경우, `domain-rules.md`에서도 함께 삭제
3. **규칙 분해** → 현재 형태로 강제 불가능하면 강제 가능한 더 작은 단위로 분해

**에이전트 행동 기준:** 새 기능 구현 시 관련 도메인 규칙의 강제 상태가 `❌ MISSING`이면,
구현 완료 전에 해당 규칙의 강제 수단을 함께 추가해야 한다.