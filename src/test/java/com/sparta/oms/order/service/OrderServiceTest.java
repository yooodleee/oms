package com.sparta.oms.order.service;

import com.sparta.oms.order.dto.OrderRequestDto;
import com.sparta.oms.order.dto.OrderResponseDto;
import com.sparta.oms.order.entity.Order;
import com.sparta.oms.order.repository.OrderRepository;
import com.sparta.oms.product.entity.Product;
import com.sparta.oms.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * OrderService 단위 테스트
 *
 * 검증 규칙:
 * @see docs/constraints/domain-rules.md (O-1: 주문 생성과 재고 차감은 원자적)
 * @see docs/constraints/domain-rules.md (P-1: 재고는 음수가 될 수 없다)
 * @see docs/adr/0002-atomic-stock-decrease.md
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("[O-1][P-1] 재고보다 많은 수량을 주문하면 예외가 발생하고 주문이 생성되지 않는다")
    void throws_exception_when_order_quantity_exceeds_stock() {
        // given: 재고 3개인 상품
        Product product = Product.builder()
                .name("재고부족상품")
                .price(1000)
                .stock(3)
                .build();
        given(productRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(product));

        OrderRequestDto request = new OrderRequestDto(1L, 5); // 재고 초과

        // when & then: 재고 부족 예외 발생
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    @DisplayName("[O-1] 정상 주문 시 재고가 차감되고 주문이 생성된다")
    void stock_is_decreased_and_order_is_created_on_success() {
        // given: 재고 10개인 상품
        Product product = Product.builder()
                .name("정상상품")
                .price(2000)
                .stock(10)
                .build();
        given(productRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(product));

        Order savedOrder = Order.create(product, 3);
        given(orderRepository.save(any())).willReturn(savedOrder);

        OrderRequestDto request = new OrderRequestDto(1L, 3);

        // when
        OrderResponseDto result = orderService.create(request);

        // then: 재고 차감 확인
        assertThat(product.getStock()).isEqualTo(7);
        assertThat(result.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("[O-1][P-2] 존재하지 않는 상품으로 주문하면 예외가 발생한다")
    void throws_exception_when_ordering_nonexistent_product() {
        // given
        given(productRepository.findByIdWithLock(99L))
                .willReturn(Optional.empty());

        OrderRequestDto request = new OrderRequestDto(99L, 1);

        // when & then
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("[P-1] 재고가 정확히 주문 수량과 같을 때 주문이 성공한다")
    void order_succeeds_when_quantity_equals_stock() {
        // given: 재고 = 주문 수량 (경계값)
        Product product = Product.builder()
                .name("경계값상품")
                .price(1000)
                .stock(5)
                .build();
        given(productRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(product));

        Order savedOrder = Order.create(product, 5);
        given(orderRepository.save(any())).willReturn(savedOrder);

        OrderRequestDto request = new OrderRequestDto(1L, 5);

        // when
        orderService.create(request);

        // then: 재고 0이 되어야 함 (음수 아님)
        assertThat(product.getStock()).isEqualTo(0);
    }
}