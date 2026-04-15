# Scripts — 자동화 도구

## 스크립트 목록

| 파일 | 목적 | 사용 시점 |
|---|---|---|
| `verify-gates.sh` | L1~L3 게이트 순차 실행 | 커밋 전, CI 실행 전 |
| `validate-docs.sh` | 문서 최신성·링크·frontmatter 검증 | 문서 추가·수정 후, 정기 품질 검사 |

## 규칙

- 모든 스크립트는 실패 시 non-zero exit code를 반환한다
- 스크립트는 환경을 파괴하지 않는다 (멱등성 보장)
- 새 스크립트 추가 시 이 README에 항목을 추가한다