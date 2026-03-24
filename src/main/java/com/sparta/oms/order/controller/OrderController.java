package com.sparta.oms.order.controller;

import com.sparta.oms.order.dto.OrderRequestDto;
import com.sparta.oms.order.dto.OrderResponseDto;
import com.sparta.oms.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public OrderResponseDto create(@RequestBody OrderRequestDto orderRequestDto) {
        return orderService.create(orderRequestDto);
    }

    // 주문 조회
    @GetMapping("/{id}")
    public OrderResponseDto get(@PathVariable Long id) {
        return orderService.get(id);
    }
}
