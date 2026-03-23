package com.sparta.oms.product.service;

import com.sparta.oms.product.dto.ProductRequestDto;
import com.sparta.oms.product.dto.ProductResponseDto;
import com.sparta.oms.product.entity.Product;
import com.sparta.oms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 생성
    public ProductResponseDto create(ProductRequestDto productRequestDto) {
        Product product = Product.builder()
                .name(productRequestDto.getName())
                .price(productRequestDto.getPrice())
                .stock(productRequestDto.getStock())
                .build();

        return new ProductResponseDto(productRepository.save(product));
    }
}
