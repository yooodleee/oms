# 메트릭 전략

## 현재 상태
미구성. Spring Boot Actuator 추가 시 아래 지표를 활성화한다.

## 목표 측정 지표

| 지표 | 목적 | SLO 연결 |
|---|---|---|
| `http.server.requests` (p99) | API 응답 지연 감지 | `docs/reliability/sla.md` |
| `orders.created.total` | 주문 생성 수 | 비즈니스 모니터링 |
| `stock.insufficient.total` | 재고 부족 발생 수 | P-1 이상 감지 |
| `jvm.memory.used` | 메모리 누수 감지 | 안정성 |

## 활성화 방법 (미구현)

```
# build.gradle에 추가 (인간 승인 필요 — Principle 1)
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

## 에이전트 행동 기준
메트릭 수집 구현은 외부 의존성 추가를 수반하므로 인간 승인 후 진행한다.
