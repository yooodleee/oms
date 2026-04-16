---
title: "제약 및 가드레일 시스템"
type: architecture
domain: cross-cutting
load_level: 1
verified_at: "2026-04-15"
references_code:
  - config/checkstyle/checkstyle.xml
  - src/test/java/com/sparta/oms/architecture/ArchitectureTest.java
  - build.gradle
related:
  - code-style.md
  - architecture-boundaries.md
  - dependency-policy.md
  - interface-contracts.md
  - ../../docs/constraints/enforcement-map.md
supersedes: null
superseded_by: null
---

# 제약 및 가드레일 시스템

에이전트가 생성한 코드가 구조적 경계를 넘지 않도록 기계적으로 차단하는 시스템.
"권장 사항"은 없다. 모든 규칙은 빌드 실패로 강제된다. (Principle 6)

---

## 가드레일 4계층

```
┌─────────────────────────────────────────────────────────────────┐
│                    GUARDRAIL SYSTEM                              │
│                                                                  │
│  Layer 1: 코드 스타일     Checkstyle   → ./gradlew checkstyle   │
│  Layer 2: 아키텍처 경계   ArchUnit     → ./gradlew test         │
│  Layer 3: 의존성 제한     ArchUnit     → ./gradlew test         │
│           + scripts/check-dependencies.sh                        │
│  Layer 4: 인터페이스 검증 ArchUnit     → ./gradlew test         │
│           + Spring Validation (@Valid)                           │
│                                                                  │
│  모든 레이어 일괄 실행: scripts/check-guardrails.sh              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 위반 탐지 → 수정 흐름

```
코드 작성
    │
    ▼
┌───────────────────────────────────────────────────────────────┐
│                    위반 탐지                                    │
│                                                               │
│  ./gradlew checkstyleMain   → 스타일 위반 위치 + 규칙 ID 출력  │
│  ./gradlew test             → ArchUnit 실패 테스트명 출력      │
│                                                               │
│  출력 예시:                                                    │
│    [checkstyle] AvoidStarImport: ProductService.java:5       │
│    [archunit] ARCH-3: product → order 의존성 감지             │
└──────────────────────┬────────────────────────────────────────┘
                       │
                       ▼
┌───────────────────────────────────────────────────────────────┐
│                    위반 분류                                    │
│                                                               │
│  스타일 위반   → 해당 파일·라인 수정 (기계적, 단순)             │
│  아키텍처 위반 → 의존성 방향 수정 (구조적, 주의 필요)           │
│  의존성 위반   → 인간 승인 후 처리 (외부 라이브러리 추가)       │
│  인터페이스 위반 → 계약 복원 (DTO 반환, @Valid 추가 등)         │
└──────────────────────┬────────────────────────────────────────┘
                       │
                       ▼
                  수정 후 재실행
              scripts/check-guardrails.sh
                       │
                  전체 통과 시
                  PR 생성 가능
```

---

## 가드레일 규칙 현황

| 규칙 ID | 계층 | 내용 | 강제 수단 | 상태 |
|---|---|---|---|---|
| STYLE-1 | 코드 스타일 | 와일드카드 임포트 금지 | Checkstyle | ✅ |
| STYLE-2 | 코드 스타일 | 명명 규칙 (클래스·메서드·변수) | Checkstyle | ✅ |
| STYLE-3 | 코드 스타일 | 빈 catch 블록 금지 | Checkstyle | ✅ |
| STYLE-4 | 코드 스타일 | 문자열 == 비교 금지 | Checkstyle | ✅ |
| ARCH-1 | 아키텍처 경계 | Controller→Repository 직접 의존 금지 | ArchUnit | ✅ |
| ARCH-2 | 아키텍처 경계 | 레이어 의존성 방향 강제 | ArchUnit | ✅ |
| ARCH-3 | 도메인 격리 | Product↔Order 도메인 격리 | ArchUnit | ✅ |
| ARCH-4 | 명명 규칙 | Controller·Service·DTO·Entity 위치·접미사 | ArchUnit | ✅ |
| ARCH-5 | 애너테이션 계약 | @Service·@RestController 필수 | ArchUnit | ✅ |
| ARCH-6 | 의존성 제한 | 테스트 라이브러리·sun.* 금지 | ArchUnit | ✅ |
| DEP-1 | 의존성 제한 | 신규 외부 라이브러리 → 인간 승인 | 정책 + 리뷰 | ⚠️ PARTIAL |
| IFACE-1 | 인터페이스 검증 | @RequestBody에 @Valid 필수 | ArchUnit (추가 필요) | ❌ MISSING |

---

## 에이전트 행동 기준

- 새 파일 생성 시 `scripts/check-guardrails.sh` 실행 필수
- 가드레일 실패를 우회(suppress, ignore)하는 것은 금지
- 규칙이 잘못됐다면 규칙 자체를 수정하고 인간에게 보고 (우회 금지)
- 새 외부 라이브러리 필요 시 → `docs/guardrails/dependency-policy.md` 참조 후 인간 승인 요청