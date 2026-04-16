---
title: "나쁜 코드 패턴 카탈로그"
type: reference
domain: cross-cutting
load_level: 2
verified_at: "2026-04-16"
references_code:
  - src/test/java/com/sparta/oms/quality/CodeQualityTest.java
  - src/main/java/com/sparta/oms/product/entity/Product.java
  - src/main/java/com/sparta/oms/order/entity/Order.java
related:
  - system.md
  - quality-metrics.md
  - tech-debt-registry.md
supersedes: null
superseded_by: null
---

# 나쁜 코드 패턴 카탈로그

이 파일은 OMS 코드베이스에서 발생했거나 발생 가능한 나쁜 패턴을 정의한다.
각 패턴은 **탐지 수단**과 **수정 방법**을 함께 제공한다.

---

## BP-1: Entity Public Setter (엔티티 public setter)

**문제:**
```java
// 금지
@Entity
public class Product {
    public void setName(String name) { this.name = name; }  // 의도가 없음
    public void setStock(int stock) { this.stock = stock; } // 검증 누락
}
```

**이유:** setter는 "어떤 규칙도 없이 값을 바꾼다"는 의미다.
재고를 직접 set하면 음수가 될 수 있고, P-1 도메인 규칙이 우회된다.

**올바른 패턴:**
```java
// 허용 — 의도가 명확하고, 규칙이 내장됨
public void decreaseStock(int quantity) {
    if (this.stock < quantity) throw new IllegalArgumentException("재고가 부족합니다.");
    this.stock -= quantity;
}
```

**탐지:** `CodeQualityTest.CQ-1` — ArchUnit이 Entity의 public set* 메서드 차단  
**강제 상태:** ✅ ENFORCED

---

## BP-2: God Service (모든 것을 아는 서비스)

**문제:**
```java
// 금지 — 한 서비스가 너무 많은 책임을 가짐
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;      // 과도한 의존
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    // 메서드가 30개 이상...
}
```

**이유:** 단일 책임 원칙 위반. 테스트가 복잡해지고 변경 영향이 커진다.

**올바른 패턴:**
- 서비스가 의존하는 Repository/Service가 3개 초과 → 분리 검토
- 메서드 수 20개 초과 → 도메인 서비스 분리 검토

**탐지:** `scripts/assess-quality.sh` — 의존성 수 분석 (현재 MISSING)  
**강제 상태:** ⚠️ PARTIAL

---

## BP-3: Raw Exception (의미 없는 예외)

**문제:**
```java
// 금지
throw new RuntimeException("뭔가 잘못됨");
throw new Exception("에러");
```

**이유:** 호출자가 예외 유형만으로 처리 방법을 알 수 없다.

**올바른 패턴:**
```java
// 허용 — 의미가 있고, 처리 방법이 명확함
throw new IllegalArgumentException("재고가 부족합니다.");
throw new IllegalStateException("이미 삭제된 상품입니다.");
// 더 나은 패턴: 도메인 예외 계층 (향후 개선 항목)
// throw new InsufficientStockException(requested, available);
```

**탐지:** `CodeQualityTest.CQ-3` — ArchUnit이 RuntimeException 직접 사용 차단  
**강제 상태:** ✅ ENFORCED

---

## BP-4: DTO-Entity 혼용 (계층 오염)

**문제:**
```java
// 금지 — DTO가 Entity 역할을 함
@Entity
@Getter
public class ProductDto {  // Dto인데 @Entity?
    @Id private Long id;
}

// 금지 — Entity를 API 응답으로 직접 반환
@GetMapping("/products/{id}")
public Product getProduct(@PathVariable Long id) {
    return productRepository.findById(id);  // 내부 구조 노출
}
```

**올바른 패턴:**
```java
@GetMapping("/products/{id}")
public ProductResponseDto getProduct(@PathVariable Long id) {
    return productService.getById(id);  // DTO로 변환 후 반환
}
```

**탐지:** `CodeQualityTest.CQ-2` — ArchUnit이 DTO에 @Entity 차단  
**강제 상태:** ✅ ENFORCED

---

## BP-5: 하드코딩 에러 메시지 (일관성 없는 오류 처리)

**현재 상태 (OMS 코드베이스):**
```java
// ProductService.java
throw new IllegalArgumentException("Product not found");

// OrderService.java
throw new IllegalArgumentException("Product not found");
throw new IllegalArgumentException("Order not found");
```

**문제:** 동일한 의미의 메시지가 여러 곳에 흩어져 있다.
오탈자 발생 가능, 테스트에서 문자열 하드코딩, 국제화(i18n) 불가.

**개선 방향:**
```java
// 상수 클래스로 추출 (향후 개선 항목)
public final class ErrorMessages {
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String ORDER_NOT_FOUND = "Order not found";
    private ErrorMessages() {}
}
```

**탐지:** `scripts/assess-quality.sh` — grep으로 중복 메시지 탐지 (PARTIAL)  
**강제 상태:** ⚠️ PARTIAL  
**등록:** `tech-debt-registry.md` TD-3

---

## BP-6: @Transactional 누락 (트랜잭션 불일치)

**현재 상태 (OMS 코드베이스):**
```java
// ProductService.java
public ProductResponseDto create(ProductRequestDto productRequestDto) {
    // @Transactional 없음 — Spring Data JPA의 save()가 자체 트랜잭션 사용
    return new ProductResponseDto(productRepository.save(product));
}
```

**문제:** 다른 write 메서드들은 `@Transactional`이 있는데 `create()`만 없다.
복잡한 create 로직이 추가될 경우 원자성이 보장되지 않는다.

**권장 수정:**
```java
@Transactional
public ProductResponseDto create(ProductRequestDto productRequestDto) { ... }
```

**탐지:** 현재 자동 탐지 수단 없음  
**강제 상태:** ❌ MISSING  
**등록:** `tech-debt-registry.md` TD-1

---

## 패턴 추가 절차

새로운 나쁜 패턴이 발견되면:
1. 이 파일에 BP-N 형식으로 패턴 정의
2. `tech-debt-registry.md`에 현재 발생 여부 기록
3. 탐지 수단이 없으면 ArchUnit 규칙 또는 스크립트 추가
4. `enforcement-map.md`에 CQ-N 항목으로 등록
