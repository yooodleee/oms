package com.sparta.oms.order.service;

import com.sparta.oms.order.dto.OrderRequestDto;
import com.sparta.oms.order.dto.OrderResponseDto;
import com.sparta.oms.order.entity.Order;
import com.sparta.oms.order.repository.OrderRepository;
import com.sparta.oms.product.entity.Product;
import com.sparta.oms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // 주문 생성
    @Transactional
    public OrderResponseDto create(OrderRequestDto orderRequestDto) {

        Product product = productRepository.findById(orderRequestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Order order = Order.create(product);

        return new OrderResponseDto(orderRepository.save(order));
    }
}
