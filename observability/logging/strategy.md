# 로깅 전략

## 현재 설정

`application.properties`:
```properties
spring.jpa.show-sql=true           # SQL 쿼리 로깅 (N+1 감지 용도)
logging.level.root=INFO
```

## 로그 레벨 기준

| 레벨 | 사용 기준 |
|---|---|
| ERROR | 복구 불가능한 오류 (DB 연결 실패 등) |
| WARN | 비즈니스 규칙 위반 시도 (재고 부족, 삭제된 상품 접근) |
| INFO | 주요 비즈니스 이벤트 (주문 생성 성공 등) |
| DEBUG | 개발·디버깅 용도만. 프로덕션 비활성화 |

## 금지 사항 (보안 정책 연동)

- 사용자 입력값 원문을 INFO 이상 레벨로 로깅 금지
- 스택 트레이스를 API 응답에 포함 금지 (`docs/security/security-policy.md`)
- 자격증명(비밀번호, 토큰)을 로그에 포함 금지

## N+1 감지 방법

SQL 로그에서 동일한 테이블에 대한 SELECT가 루프 내에서 반복되면 N+1이다.
```
# N+1 패턴 (금지)
SELECT * FROM product WHERE id=1
SELECT * FROM product WHERE id=2
SELECT * FROM product WHERE id=3
...

# 올바른 패턴 (JOIN FETCH)
SELECT o.*, p.* FROM orders o JOIN product p ON o.product_id = p.id
```
