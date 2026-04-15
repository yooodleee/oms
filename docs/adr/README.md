# Architecture Decision Records

이 디렉토리는 OMS 프로젝트의 아키텍처 결정 근거를 저장합니다.

에이전트는 새 기능을 구현하기 전에 이 디렉토리를 읽어 기존 결정과 충돌하지 않는지 확인합니다.

## 목록

| ADR | 제목 | 상태 |
|---|---|---|
| [0001](0001-soft-delete-for-products.md) | 상품 소프트 삭제 도입 | Accepted |
| [0002](0002-atomic-stock-decrease.md) | 재고 차감 원자성 보장 | Accepted |
| [0003](0003-join-fetch-for-orders.md) | 주문 조회 JOIN FETCH | Accepted |

## ADR 작성 기준

- 결정이 **되돌리기 어렵거나** 다른 에이전트가 실수할 수 있는 지점이면 반드시 ADR을 작성한다
- 파일명: `NNNN-kebab-case-title.md`
- 상태 변경 시 기존 파일을 수정하고 `Superseded by ADR-XXXX`를 명시한다
