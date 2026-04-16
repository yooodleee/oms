package com.sparta.oms.order.controller;

import com.sparta.oms.order.dto.OrderListResponseDto;
import com.sparta.oms.order.dto.OrderRequestDto;
import com.sparta.oms.order.dto.OrderResponseDto;
import com.sparta.oms.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 페이지네이션 조회
    @GetMapping
    public Page<OrderListResponseDto> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return orderService.getOrders(pageable);
    }

    // 주문 생성
    @PostMapping
    public OrderResponseDto create(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        return orderService.create(orderRequestDto);
    }

    // 주문 조회
    @GetMapping("/{id}")
    public OrderResponseDto get(@PathVariable Long id) {
        return orderService.get(id);
    }
}
