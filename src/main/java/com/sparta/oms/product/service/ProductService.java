package com.sparta.oms.product.service;

import com.sparta.oms.product.dto.ProductRequestDto;
import com.sparta.oms.product.dto.ProductResponseDto;
import com.sparta.oms.product.entity.Product;
import com.sparta.oms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 생성
    public ProductResponseDto create(ProductRequestDto productRequestDto) {
        Product product = Product.create(
                productRequestDto.getName(),
                productRequestDto.getPrice(),
                productRequestDto.getStock()
        );

        return new ProductResponseDto(productRepository.save(product));
    }

    // 단건 조회
    public ProductResponseDto getById(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        return new ProductResponseDto(product);
    }

    // 목록 조회
    public List<ProductResponseDto> getAll() {
        return productRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(ProductResponseDto::new)
                .toList();
    }

    // 수정
    @Transactional
    public ProductResponseDto update(Long id, ProductRequestDto productRequestDto) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.update(
                productRequestDto.getName(),
                productRequestDto.getPrice(),
                productRequestDto.getStock()
        );

        return new ProductResponseDto(productRepository.save(product));
    }

    // 소프트 삭제
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.softDelete();
    }
}
