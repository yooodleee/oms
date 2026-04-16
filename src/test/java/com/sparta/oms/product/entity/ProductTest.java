package com.sparta.oms.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("[P-3] 상품 가격·재고는 0 이상이어야 한다")
    class PriceAndStockValidationTests {

        @Test
        @DisplayName("[P-3] 가격이 음수이면 상품 생성 시 IllegalArgumentException을 던진다")
        void create_fails_when_price_is_negative() {
            assertThatThrownBy(() -> Product.create("상품A", -1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("가격은 0 이상이어야 합니다.");
        }

        @Test
        @DisplayName("[P-3] 재고가 음수이면 상품 생성 시 IllegalArgumentException을 던진다")
        void create_fails_when_stock_is_negative() {
            assertThatThrownBy(() -> Product.create("상품A", 1000, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고는 0 이상이어야 합니다.");
        }

        @Test
        @DisplayName("[P-3] 가격 0, 재고 0은 허용된다")
        void create_succeeds_when_price_and_stock_are_zero() {
            Product product = Product.create("무료상품", 0, 0);

            assertThat(product.getPrice()).isEqualTo(0);
            assertThat(product.getStock()).isEqualTo(0);
        }
    }
}
