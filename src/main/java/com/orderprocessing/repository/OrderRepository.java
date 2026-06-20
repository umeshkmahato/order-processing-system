package com.orderprocessing.repository;

import com.orderprocessing.entity.Order;
import com.orderprocessing.entity.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}

