package com.sparta.oms.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderRequestDto {

    @NotNull
    private Long productId;

    @Min(1)
    private int quantity;
}
