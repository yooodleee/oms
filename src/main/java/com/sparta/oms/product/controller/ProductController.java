package com.sparta.oms.product.controller;

import com.sparta.oms.product.dto.ProductRequestDto;
import com.sparta.oms.product.dto.ProductResponseDto;
import com.sparta.oms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 단건 조회
    @GetMapping("{id}")
    public ProductResponseDto get(@PathVariable Long id) {
        return productService.getById(id);
    }

    // 목록 조회
    @GetMapping
    public List<ProductResponseDto> getAll() {
        return productService.getAll();
    }

    // 수정
    @PutMapping("/{id}")
    public ProductResponseDto update(
            @PathVariable Long id,
            @RequestBody ProductRequestDto productRequestDto
    ) {
        return productService.update(id, productRequestDto);
    }
}
