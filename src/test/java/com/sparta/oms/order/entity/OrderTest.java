package com.sparta.oms.order.entity;

import com.sparta.oms.product.entity.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void create_주문생성시_상품가격이_설정된다() {
        Product product = Product.builder()
                .name("상품A")
                .price(1500)
                .stock(10)
                .build();

        Order order = Order.create(product, 3);

        assertThat(order.getProduct()).isEqualTo(product);
        assertThat(order.getQuantity()).isEqualTo(3);
        assertThat(order.getPrice()).isEqualTo(1500);
    }

    @Test
    void create_재고차감_후_주문생성() {
        Product product = Product.builder()
                .name("상품A")
                .price(1000)
                .stock(10)
                .build();

        product.decreaseStock(4);
        Order order = Order.create(product, 4);

        assertThat(product.getStock()).isEqualTo(6);
        assertThat(order.getQuantity()).isEqualTo(4);
        assertThat(order.getPrice()).isEqualTo(1000);
    }
}
