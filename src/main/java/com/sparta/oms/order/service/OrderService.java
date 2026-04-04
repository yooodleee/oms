package com.sparta.oms.order.service;

import com.sparta.oms.order.dto.OrderListResponseDto;
import com.sparta.oms.order.dto.OrderRequestDto;
import com.sparta.oms.order.dto.OrderResponseDto;
import com.sparta.oms.order.entity.Order;
import com.sparta.oms.order.repository.OrderRepository;
import com.sparta.oms.product.entity.Product;
import com.sparta.oms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // 페이지네이션 조회
    public Page<OrderListResponseDto> getOrders(Pageable pageable) {

        Page<Order> orders = orderRepository.findAllWithProduct(pageable);

        return orders.map(OrderListResponseDto::new);
    }

    // 주문 생성
    @Transactional
    public OrderResponseDto create(OrderRequestDto orderRequestDto) {

        // 비관적 락으로 상품 조회 후, 재고 차감은 엔티티 도메인 로직에 위임
        Product product = productRepository.findByIdWithLock(orderRequestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.decreaseStock(orderRequestDto.getQuantity());

        Order order = Order.create(product, orderRequestDto.getQuantity());

        return new OrderResponseDto(orderRepository.save(order));
    }

    // 단건 조회
    public OrderResponseDto get(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        return new OrderResponseDto(order);
    }
}
