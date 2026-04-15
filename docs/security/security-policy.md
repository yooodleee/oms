# 보안 정책

## 현재 보안 범위

인증·인가는 현재 구현 범위 밖이다 (`docs/product/requirements.md` 참조).

## 에이전트 금지 행동

| 항목 | 이유 |
|---|---|
| SQL 직접 문자열 조합 (`"WHERE id = " + id`) | SQL Injection |
| 사용자 입력을 로그에 그대로 출력 | 민감정보 노출 |
| 스택 트레이스를 API 응답에 포함 | 내부 구조 노출 |
| 하드코딩된 자격증명 | 자격증명 탈취 |

## 환경 변수 관리

민감 정보는 반드시 환경 변수로 분리한다:
```
DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD
```
코드·설정 파일에 직접 기재 금지. `.gitignore`에 `.env` 포함 확인.

## 보안 강제 수단

| 위협 | 강제 수단 | 상태 |
|---|---|---|
| SQL Injection | JPA 파라미터 바인딩 전용 (`@Query` + `:param`) | ✅ 구조적 강제 |
| 스택 트레이스 노출 | `@ExceptionHandler` 전역 처리 | ❌ MISSING |
| 민감정보 로그 노출 | 로그 정책 문서 + 코드 리뷰 | ⚠️ PARTIAL |

## 에이전트 행동 기준

새 Repository 쿼리 추가 시:
- 문자열 연결로 쿼리를 조합하는 코드는 `ArchitectureTest`에 금지 규칙을 추가한다
- 네이티브 쿼리 사용 시 반드시 파라미터 바인딩(`:param`)만 허용한다
