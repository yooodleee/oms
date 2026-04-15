package com.sparta.oms.product.service;

import com.sparta.oms.product.dto.ProductRequestDto;
import com.sparta.oms.product.dto.ProductResponseDto;
import com.sparta.oms.product.entity.Product;
import com.sparta.oms.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ProductService 단위 테스트
 *
 * 검증 규칙:
 * @see docs/constraints/domain-rules.md (P-2: 삭제된 상품은 조회 대상에서 제외)
 * @see docs/adr/0001-soft-delete-for-products.md
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 단위 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("[P-2] 소프트 삭제된 상품을 조회하면 예외가 발생한다")
    void throws_exception_when_getting_soft_deleted_product() {
        // given
        given(productRepository.findByIdAndDeletedAtIsNull(1L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("[P-2] 존재하는 활성 상품은 정상 조회된다")
    void returns_product_when_active_product_exists() {
        // given
        Product activeProduct = Product.builder()
                .name("활성상품")
                .price(1000)
                .stock(10)
                .build();
        given(productRepository.findByIdAndDeletedAtIsNull(1L))
                .willReturn(Optional.of(activeProduct));

        // when
        ProductResponseDto result = productService.getById(1L);

        // then
        assertThat(result.getName()).isEqualTo("활성상품");
    }

    @Test
    @DisplayName("[P-2] 소프트 삭제 요청 시 softDelete()가 호출된다")
    void soft_delete_calls_softDelete_on_product() {
        // given
        Product product = Product.builder()
                .name("삭제될상품")
                .price(1000)
                .stock(5)
                .build();
        given(productRepository.findByIdAndDeletedAtIsNull(1L))
                .willReturn(Optional.of(product));

        // when
        productService.delete(1L);

        // then: softDelete() 호출로 deletedAt이 설정됐는지 간접 검증
        assertThat(product.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("[P-2] 이미 삭제된 상품 재삭제 시도 시 예외가 발생한다")
    void throws_exception_when_deleting_already_deleted_product() {
        // given
        given(productRepository.findByIdAndDeletedAtIsNull(99L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("[P-2] 존재하지 않는 상품 수정 시 예외가 발생한다")
    void throws_exception_when_updating_nonexistent_product() {
        // given
        given(productRepository.findByIdAndDeletedAtIsNull(99L))
                .willReturn(Optional.empty());
        ProductRequestDto request = new ProductRequestDto("수정명", 2000, 20);

        // when & then
        assertThatThrownBy(() -> productService.update(99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found");
    }
}