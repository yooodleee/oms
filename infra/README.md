# Infra — CI/CD 및 배포

## 디렉토리 구조

```
infra/
├── ci/       ← CI 파이프라인 설정
└── deploy/   ← 배포 절차 및 환경 설정
```

## CI 게이트 (Principle 6 — 강제 수단)

CI 파이프라인은 아래 순서로 게이트를 실행한다. 하나라도 실패하면 merge 차단.

```
1. ./gradlew compileJava    (L1: 컴파일)
2. ./gradlew test           (L2: 단위 테스트 + ArchUnit)
3. ./gradlew build          (L3: 전체 빌드)
```

## 필수 환경 변수

```
DATABASE_URL=jdbc:mysql://localhost:3306/<dbname>
DATABASE_USERNAME=<user>
DATABASE_PASSWORD=<password>
```

CI 환경에서는 Secrets으로 관리한다. 코드에 직접 기재 금지 (`docs/security/security-policy.md` 참조).

## 에이전트 행동 기준

- CI 설정 변경 시 반드시 로컬에서 전체 게이트를 먼저 통과시킨다
- 게이트 순서를 변경하거나 특정 게이트를 건너뛰는 구성은 금지한다