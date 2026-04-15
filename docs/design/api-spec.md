# API 명세

Base URL: `http://localhost:8084`

---

## Product API

### 상품 목록 조회
```
GET /products
Response 200: [ { id, name, price, stock } ]
```

### 상품 단건 조회
```
GET /products/{id}
Response 200: { id, name, price, stock }
Response 404: 상품 없음 또는 삭제된 상품 (P-2)
```

### 상품 생성
```
POST /products
Body: { name: string, price: int, stock: int }
Response 201: { id, name, price, stock }
```

### 상품 수정
```
PUT /products/{id}
Body: { name: string, price: int, stock: int }
Response 200: { id, name, price, stock }
```

### 상품 삭제 (소프트 삭제)
```
DELETE /products/{id}
Response 204
비고: 물리적 삭제 아님. deletedAt 설정 (ADR-0001)
```

---

## Order API

### 주문 생성
```
POST /orders
Body: { productId: long, quantity: int }
Response 201: { id, productId, productName, quantity, price, createdAt }
Response 400: 재고 부족 (P-1)
Response 404: 상품 없음 또는 삭제된 상품 (P-2)
```

### 주문 목록 조회 (페이지네이션)
```
GET /orders?page=0&size=10
Response 200: {
  content: [ { id, productId, productName, quantity, price, createdAt } ],
  page, size, totalElements, totalPages
}
비고: JOIN FETCH로 단일 쿼리 처리 (ADR-0003)
```

---

## 에이전트 주의사항
- 새 엔드포인트 추가 시 이 파일을 함께 갱신한다
- 응답 형식 변경 시 기존 클라이언트 호환성을 `constraints` 항목에 명시한다
