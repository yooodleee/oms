package com.sparta.oms.product.dto;

import com.sparta.oms.product.entity.Product;
import lombok.Getter;

@Getter
public class ProductResponseDto {

    private Long id;
    private String name;
    private int price;
    private int stock;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
    }
}
