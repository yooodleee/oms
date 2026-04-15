# Tests — 테스트 전략

실제 테스트 코드는 `src/test/`에 위치한다. 이 디렉토리는 **테스트 전략, 기준, 커버리지 목표**를 정의한다.

## 테스트 레이어

| 레이어 | 위치 | 목적 |
|---|---|---|
| 단위 테스트 | `src/test/.../entity/`, `.../service/` | 도메인 규칙 검증 |
| 아키텍처 테스트 | `src/test/.../architecture/ArchitectureTest.java` | 레이어 의존성 강제 |
| 통합 테스트 | (추후 추가) | 실제 DB와 함께 E2E 검증 |

## 명명 기준 (Principle 5)

```java
// 형식: [규칙ID] 시나리오 → 기대결과
@DisplayName("[P-1] 재고가 0일 때 주문 생성은 InsufficientStockException을 던진다")
void throws_InsufficientStockException_when_stock_is_zero() { }
```

## 커버리지 목표

| 규칙 ID | 테스트 존재 여부 | 위치 |
|---|---|---|
| P-1 | ✅ | `ProductTest.decreaseStock_재고_부족_예외발생` |
| P-2 | ❌ | 추가 필요 |
| P-3 | ❌ | 추가 필요 |
| O-1 | ❌ | 추가 필요 |
| O-3 | ❌ | 추가 필요 |
| ARCH-1 | ✅ | `ArchitectureTest` |
| ARCH-2 | ✅ | `ArchitectureTest` |

`❌` 항목은 `docs/constraints/enforcement-map.md`의 `MISSING` 항목과 동기화된다.

## 실행 명령

```bash
./gradlew test                              # 전체
./gradlew test --tests "*.ArchitectureTest" # 아키텍처만
./gradlew test --tests "*.ProductTest"      # 상품 도메인만
```