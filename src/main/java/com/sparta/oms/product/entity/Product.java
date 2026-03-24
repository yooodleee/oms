package com.sparta.oms.product.entity;

import jakarta.persistence.*;
import lombok.*;

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

    public void update(String name, int price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // 재고 차감
//    public void decreaseStock(int quantity) {
//        if (this.stock < quantity) {
//            throw new IllegalArgumentException("Out of stock");
//        }
//        this.stock -= quantity;
//    }
}
