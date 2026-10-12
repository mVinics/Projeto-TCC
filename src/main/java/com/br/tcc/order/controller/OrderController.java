package com.br.tcc.order.controller;

import com.br.tcc.order.dto.*;
import com.br.tcc.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.create(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(
            @PathVariable UUID orderId
    ) {
        return orderService.getById(orderId);
    }

    @PostMapping("/{orderId}/items")
    public OrderResponse addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddOrderItemRequest request
    ) {
        return orderService.addItem(orderId, request);
    }

    @PatchMapping("/{orderId}/items/{productId}")
    public OrderResponse updateItem(
            @PathVariable UUID orderId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateOrderItemRequest request
    ) {
        return orderService.updateItem(
                orderId,
                productId,
                request
        );
    }

    @DeleteMapping("/{orderId}/items/{productId}")
    public OrderResponse removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID productId
    ) {
        return orderService.removeItem(
                orderId,
                productId
        );
    }

    @PostMapping("/{orderId}/checkout")
    public OrderResponse checkout(
            @PathVariable UUID orderId
    ) {
        return orderService.checkout(orderId);
    }

    @PostMapping("/{orderId}/coupons/{code}")
    public OrderResponse applyCoupon(
            @PathVariable UUID orderId,
            @PathVariable String code
    ) {
        return orderService.applyCoupon(orderId, code);
    }

    @DeleteMapping("/{orderId}/coupon")
    public OrderResponse removeCoupon(
            @PathVariable UUID orderId
    ) {
        return orderService.removeCoupon(orderId);
    }

    @PostMapping("/{orderId}/payment")
    public OrderResponse pay(
            @PathVariable UUID orderId,
            @Valid @RequestBody PaymentRequest request
    ) {
        return orderService.pay(orderId, request);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(
            @PathVariable UUID orderId
    ) {
        return orderService.cancel(orderId);
    }

    @PostMapping("/{orderId}/shipment")
    public OrderResponse ship(
            @PathVariable UUID orderId
    ) {
        return orderService.ship(orderId);
    }
}