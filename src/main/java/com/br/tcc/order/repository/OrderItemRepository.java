package com.br.tcc.order.repository;

import com.br.tcc.order.entity.OrderItem;
import com.br.tcc.order.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, OrderItemId> {
}