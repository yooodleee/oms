package com.sparta.oms.order.repository;

import com.sparta.oms.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(
            value = "select o from Order o join fetch o.product",
            countQuery = "select count(o) from Order o"
    )
    Page<Order> findAllWithProduct(Pageable pageable);
}
