package com.sparta.oms.product.repository;

import com.sparta.oms.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository  extends JpaRepository<Product, Long> {
    // Atomic Update
    @Modifying(clearAutomatically = true)
    @Query("""
    update Product p
    set p.stock = p.stock - :quantity
    where p.id = :id and p.stock >= :quantity
""")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
