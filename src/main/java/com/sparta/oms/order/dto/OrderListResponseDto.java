package com.sparta.oms.order.dto;

import com.sparta.oms.order.entity.Order;
import lombok.Getter;

@Getter
public class OrderListResponseDto {

    private Long orderId;
    private Long productId;
    private String productName;
    private int quantity;
    private int price;

    public OrderListResponseDto(Order order) {
        this.orderId = order.getId();
        this.productId = order.getProduct().getId();
        this.productName = order.getProduct().getName();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
    }
}
