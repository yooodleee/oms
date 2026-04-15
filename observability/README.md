# Observability — 로그, 메트릭, 트레이싱

## 목적

에이전트와 인간이 시스템 상태를 **코드를 읽지 않고도** 파악할 수 있어야 한다.
Observability는 Principle 4(피드백 루프)의 "Observe" 단계를 지원한다.

## 디렉토리 구조

```
observability/
├── logging/    ← 로그 전략 및 기준
├── metrics/    ← 측정 지표 정의
└── tracing/    ← 분산 추적 전략
```

## 현재 구성

| 항목 | 현재 상태 | 위치 |
|---|---|---|
| SQL 로그 | ✅ 활성화 (`show-sql=true`) | `application.properties` |
| 애플리케이션 로그 | ⚠️ 기본값 (전략 없음) | `observability/logging/` |
| 메트릭 | ❌ 미구성 | `observability/metrics/` |
| 트레이싱 | ❌ 미구성 | `observability/tracing/` |

## 에이전트 활용 기준

- **N+1 감지**: SQL 로그에서 동일 쿼리 반복 → L5 게이트 위반 (ADR-0003)
- **재고 이상**: 로그에서 `InsufficientStockException` 패턴 → P-1 위반 알람
- **응답 지연**: 메트릭에서 p99 > 기준값 → `docs/reliability/sla.md` 참조