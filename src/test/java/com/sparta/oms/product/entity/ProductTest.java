package com.sparta.oms.product.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void decreaseStock_성공() {
        Product product = Product.builder()
                .name("상품A")
                .price(1000)
                .stock(10)
                .build();

        product.decreaseStock(3);

        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    void decreaseStock_재고와_동일한_수량_차감_성공() {
        Product product = Product.builder()
                .name("상품A")
                .price(1000)
                .stock(5)
                .build();

        product.decreaseStock(5);

        assertThat(product.getStock()).isEqualTo(0);
    }

    @Test
    void decreaseStock_재고_부족_예외발생() {
        Product product = Product.builder()
                .name("상품A")
                .price(1000)
                .stock(2)
                .build();

        assertThatThrownBy(() -> product.decreaseStock(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    void create_정적팩토리_객체생성() {
        Product product = Product.create("상품B", 2000, 20);

        assertThat(product.getName()).isEqualTo("상품B");
        assertThat(product.getPrice()).isEqualTo(2000);
        assertThat(product.getStock()).isEqualTo(20);
    }
}
