package com.coffee.pos.repository;

import com.coffee.pos.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, Order.OrderStatus status);
}