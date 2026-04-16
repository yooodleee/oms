package com.sparta.oms.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column
    private LocalDateTime deletedAt;

    /**
     * @see docs/constraints/domain-rules.md (P-3)
     */
    public static Product create(String name, int price, int stock) {
        validatePriceAndStock(price, stock);
        return Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
    }

    /**
     * @see docs/constraints/domain-rules.md (P-3)
     */
    public void update(String name, int price, int stock) {
        validatePriceAndStock(price, stock);
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    private static void validatePriceAndStock(int price, int stock) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 재고 차감 (도메인 로직: 재고 부족 검증 포함)
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}
