package com.sparta.oms.product.controller;

import com.sparta.oms.product.dto.ProductRequestDto;
import com.sparta.oms.product.dto.ProductResponseDto;
import com.sparta.oms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 생성
    @PostMapping
    public ProductResponseDto create(@RequestBody ProductRequestDto productRequestDto) {
        return productService.create(productRequestDto);
    }
}
