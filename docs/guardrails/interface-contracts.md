---
title: "인터페이스 검증 계약"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - src/main/java/com/sparta/oms/product/controller/ProductController.java
  - src/main/java/com/sparta/oms/order/controller/OrderController.java
  - src/main/java/com/sparta/oms/product/dto/ProductRequestDto.java
  - src/main/java/com/sparta/oms/order/dto/OrderRequestDto.java
related:
  - system.md
  - ../../docs/design/api-spec.md
supersedes: null
superseded_by: null
---

# 인터페이스 검증 계약

에이전트가 새 API 엔드포인트나 DTO를 추가할 때 반드시 지켜야 할 계약.

---

## Controller 계약

### 규칙 C-1: @RequestBody는 @Valid 필수
```java
// 금지 (위반)
public ResponseEntity<?> create(@RequestBody OrderRequestDto request) { }

// 허용
public ResponseEntity<?> create(@Valid @RequestBody OrderRequestDto request) { }
```
**이유:** @Valid 없이는 Bean Validation이 실행되지 않아 잘못된 입력이 서비스 레이어까지 전달된다.

### 규칙 C-2: HTTP 상태 코드는 api-spec.md와 일치해야 한다
```java
// 생성: 201 Created
return ResponseEntity.status(HttpStatus.CREATED).body(dto);

// 삭제: 204 No Content
return ResponseEntity.noContent().build();

// 조회 성공: 200 OK (기본값)
```

### 규칙 C-3: 예외는 전역 ExceptionHandler에서 처리한다
```java
// Controller에서 직접 try-catch로 응답 만들기 금지
// @RestControllerAdvice 에서 일관되게 처리
```

---

## DTO 계약

### 규칙 D-1: 요청 DTO는 Bean Validation 애너테이션 필수
```java
public class OrderRequestDto {
    @NotNull
    private Long productId;      // null 금지

    @Min(1)
    private int quantity;        // 1 이상
}
```

### 규칙 D-2: 응답 DTO는 Entity를 직접 노출하지 않는다
```java
// 금지: Entity 직접 반환
public Product getProduct() { return productRepository.findById(id); }

// 허용: DTO로 변환
public ProductResponseDto getProduct() { return new ProductResponseDto(product); }
```

### 규칙 D-3: DTO는 dto 패키지에 위치해야 한다 (ARCH-4 연동)

---

## Repository 계약

### 규칙 R-1: 단건 조회는 Optional 반환 필수
```java
// 금지: null 반환 가능
Product findById(Long id);

// 허용: null 안전
Optional<Product> findById(Long id);
Optional<Product> findByIdAndDeletedAtIsNull(Long id);
```

### 규칙 R-2: 소프트 삭제 엔티티 조회 시 deletedAt IS NULL 조건 필수
```java
// 금지 (ADR-0001 위반)
Optional<Product> findById(Long id);

// 허용
Optional<Product> findByIdAndDeletedAtIsNull(Long id);
```

---

## 위반 탐지 및 수정

| 위반 유형 | 탐지 방법 | 수정 |
|---|---|---|
| @Valid 누락 | ArchUnit (IFACE-1, 추가 예정) + E2E 테스트 | @Valid 추가 |
| HTTP 상태 코드 불일치 | E2E 테스트 (api-spec.md 기반) | api-spec.md 먼저 확인 후 수정 |
| Entity 직접 반환 | ArchUnit (추가 예정) | DTO 변환 레이어 추가 |
| Optional 미사용 | ArchUnit (추가 예정) | 반환 타입 변경 |
| deletedAt 조건 누락 | 통합 테스트 (P-2 강제) | 쿼리 조건 추가 |

---

## 현재 강제 상태

| 계약 | 강제 수단 | 상태 |
|---|---|---|
| @Valid 필수 (C-1) | ArchUnit IFACE-1 | ❌ MISSING |
| HTTP 상태 코드 (C-2) | E2E 테스트 | ❌ MISSING |
| Optional 반환 (R-1) | ArchUnit | ❌ MISSING |
| deletedAt 조건 (R-2) | 통합 테스트 | ❌ MISSING |
