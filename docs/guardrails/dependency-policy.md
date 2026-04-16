---
title: "의존성 제한 정책"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - build.gradle
related:
  - system.md
supersedes: null
superseded_by: null
---

# 의존성 제한 정책

**원칙:** 외부 의존성 추가는 에이전트가 단독으로 결정할 수 없다. 반드시 인간의 명시적 승인이 필요하다. (Principle 1)

---

## 승인된 의존성 목록 (현재)

| 의존성 | 용도 | 범위 |
|---|---|---|
| `spring-boot-starter-data-jpa` | ORM, 데이터 접근 | implementation |
| `spring-boot-starter-validation` | Bean Validation | implementation |
| `spring-boot-starter-web` | HTTP 레이어 | implementation |
| `lombok` | 보일러플레이트 제거 | compileOnly |
| `mysql-connector-j` | MySQL 드라이버 | runtimeOnly |
| `archunit-junit5` | 아키텍처 테스트 | testImplementation |
| `junit-platform-launcher` | 테스트 런처 | testRuntimeOnly |

---

## 의존성 추가 절차 (인간 승인 필요)

```
에이전트가 새 의존성이 필요하다고 판단한 경우:

1. 작업을 ESCALATED 상태로 전환
2. 아래 형식으로 인간에게 승인 요청:

   의존성 추가 요청:
     라이브러리: <groupId:artifactId:version>
     필요 이유:  <구체적 필요성>
     범위:       implementation | testImplementation | compileOnly
     대안 검토:  <기존 승인된 라이브러리로 해결 불가한 이유>
     보안 고려:  <알려진 취약점 여부>

3. 인간이 승인하면:
   - build.gradle에 추가
   - 이 파일의 승인 목록에 추가
   - docs/adr/에 결정 기록 (의존성 선택은 아키텍처 결정)
```

---

## 금지 패턴

| 패턴 | 이유 |
|---|---|
| 테스트 라이브러리를 `implementation` 범위에 추가 | 배포 아티팩트에 포함됨 |
| `sun.*` 패키지 직접 사용 | JDK 내부 API, 버전 간 호환성 없음 |
| 직접 JDBC/SQL 라이브러리 추가 | JPA가 있으므로 불필요, N+1 위험 |
| 버전 고정 없이 `latest` 사용 | 재현 불가능한 빌드 |
| 라이선스 미확인 라이브러리 | 법적 리스크 |

---

## 위반 탐지

```bash
# ARCH-6: 테스트 라이브러리가 production 코드에서 사용되는지 확인
./gradlew test --tests "*.ArchitectureTest"

# 수동 의존성 감사 (승인 목록과 비교)
./gradlew dependencies --configuration runtimeClasspath
```
