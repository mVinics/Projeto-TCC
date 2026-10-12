package com.br.tcc.order.dto;

import com.br.tcc.order.entity.Order;
import com.br.tcc.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String couponCode,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getTotal(),
                order.getCoupon() != null
                        ? order.getCoupon().getCode()
                        : null,
                order.getItems()
                        .stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}