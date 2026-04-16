package com.sparta.oms.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProductRequestDto {

    @NotBlank
    private String name;

    @Min(0)
    private int price;

    @Min(0)
    private int stock;
}
