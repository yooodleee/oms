package com.sparta.oms.product.repository;

import com.sparta.oms.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository  extends JpaRepository<Product, Long> {
}
