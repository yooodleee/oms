package com.sparta.oms.product.controller;

import com.sparta.oms.product.dto.ProductRequestDto;
import com.sparta.oms.product.dto.ProductResponseDto;
import com.sparta.oms.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 생성
    @PostMapping
    public ProductResponseDto create(@Valid @RequestBody ProductRequestDto productRequestDto) {
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
            @Valid @RequestBody ProductRequestDto productRequestDto
    ) {
        return productService.update(id, productRequestDto);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
