package com.sparta.oms.order.dto;

import lombok.Getter;

@Getter
public class OrderRequestDto {
    private Long productId;
    private int quantity;
}
