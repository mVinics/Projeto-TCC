package com.br.tcc.order.service;

import com.br.tcc.coupon.entity.Coupon;
import com.br.tcc.coupon.entity.CouponStatus;
import com.br.tcc.coupon.repository.CouponRepository;
import com.br.tcc.order.dto.PaymentRequest;
import com.br.tcc.order.payment.entity.Payment;
import com.br.tcc.order.payment.repository.PaymentRepository;
import com.br.tcc.order.shipment.entity.Shipment;
import com.br.tcc.order.shipment.repository.ShipmentRepository;
import com.br.tcc.order.dto.AddOrderItemRequest;
import com.br.tcc.order.entity.OrderItem;
import com.br.tcc.order.entity.OrderStatus;
import com.br.tcc.product.entity.Product;
import com.br.tcc.product.entity.ProductStatus;
import com.br.tcc.product.repository.ProductRepository;
import com.br.tcc.shared.exception.ResourceConflictException;
import com.br.tcc.customer.entity.Customer;
import com.br.tcc.customer.entity.CustomerStatus;
import com.br.tcc.customer.repository.CustomerRepository;
import com.br.tcc.order.dto.CreateOrderRequest;
import com.br.tcc.order.dto.OrderResponse;
import com.br.tcc.order.entity.Order;
import com.br.tcc.order.repository.OrderRepository;
import com.br.tcc.shared.exception.BusinessRuleViolationException;
import com.br.tcc.shared.exception.ResourceNotFoundException;
import com.br.tcc.order.dto.UpdateOrderItemRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            CouponRepository couponRepository,
            PaymentRepository paymentRepository,
            ShipmentRepository shipmentRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + request.customerId()
                ));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessRuleViolationException(
                    "Only active customers can create orders"
            );
        }

        Order order = new Order(customer);

        Order savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID orderId) {
        Order order = findOrder(orderId);

        return OrderResponse.from(order);
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId
                ));
    }

    @Transactional
    public OrderResponse addItem(
            UUID orderId,
            AddOrderItemRequest request
    ) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Items can only be added while the order is in DRAFT status"
            );
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + request.productId()
                ));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleViolationException(
                    "Only active products can be added to an order"
            );
        }

        if (request.quantity() <= 0) {
            throw new BusinessRuleViolationException(
                    "Item quantity must be greater than zero"
            );
        }

        if (request.quantity() > product.getStock()) {
            throw new BusinessRuleViolationException(
                    "Requested quantity exceeds available stock"
            );
        }

        boolean productAlreadyExists = order.getItems()
                .stream()
                .anyMatch(item ->
                        item.getProduct()
                                .getId()
                                .equals(product.getId())
                );

        if (productAlreadyExists) {
            throw new ResourceConflictException(
                    "Product already exists in the order"
            );
        }

        OrderItem item = new OrderItem(
                order,
                product,
                request.quantity(),
                product.getPrice()
        );

        order.addItem(item);

        recalculateTotals(order);

        return OrderResponse.from(order);
    }

    private void recalculateTotals(Order order) {
        BigDecimal subtotal = order.getItems()
                .stream()
                .map(item ->
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);

        BigDecimal discount = order.getCoupon() != null
                ? order.getCoupon().getDiscountValue()
                : BigDecimal.ZERO;

        order.setDiscount(discount);
        order.setTotal(subtotal.subtract(discount));
    }

    @Transactional
    public OrderResponse updateItem(
            UUID orderId,
            UUID productId,
            UpdateOrderItemRequest request
    ) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Items can only be modified while the order is in DRAFT status"
            );
        }

        if (request.quantity() <= 0) {
            throw new BusinessRuleViolationException(
                    "Item quantity must be greater than zero"
            );
        }

        OrderItem item = order.getItems()
                .stream()
                .filter(current ->
                        current.getProduct()
                                .getId()
                                .equals(productId)
                )
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in order: " + productId
                ));

        Product product = item.getProduct();

        if (request.quantity() > product.getStock()) {
            throw new BusinessRuleViolationException(
                    "Requested quantity exceeds available stock"
            );
        }

        item.setQuantity(request.quantity());

        recalculateTotals(order);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse removeItem(
            UUID orderId,
            UUID productId
    ) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Items can only be removed while the order is in DRAFT status"
            );
        }

        OrderItem item = order.getItems()
                .stream()
                .filter(current ->
                        current.getProduct()
                                .getId()
                                .equals(productId)
                )
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in order: " + productId
                ));

        order.removeItem(item);

        recalculateTotals(order);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse checkout(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getItems().isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Order must have at least one item to checkout"
            );
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();

            if (item.getQuantity() > product.getStock()) {
                throw new BusinessRuleViolationException(
                        "Insufficient stock for product: " + product.getId()
                );
            }
        }

        order.setStatus(OrderStatus.PENDING_PAYMENT);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse applyCoupon(
            UUID orderId,
            String code
    ) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Coupon can only be applied while the order is in DRAFT status"
            );
        }

        if (order.getCoupon() != null) {
            throw new BusinessRuleViolationException(
                    "Only one coupon can be applied to an order"
            );
        }

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coupon not found: " + code
                ));

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        boolean outsideValidityPeriod =
                today.isBefore(coupon.getValidFrom())
                        || today.isAfter(coupon.getValidUntil());

        if (coupon.getStatus() != CouponStatus.ACTIVE
                || outsideValidityPeriod) {
            throw new BusinessRuleViolationException(
                    "Coupon must be active and within its validity period"
            );
        }

        if (order.getSubtotal()
                .compareTo(coupon.getMinimumOrderValue()) < 0) {
            throw new BusinessRuleViolationException(
                    "Order does not meet the minimum value required by the coupon"
            );
        }

        order.setCoupon(coupon);
        order.setDiscount(coupon.getDiscountValue());
        order.setTotal(
                order.getSubtotal().subtract(coupon.getDiscountValue())
        );

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse removeCoupon(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Coupon can only be removed while the order is in DRAFT status"
            );
        }

        if (order.getCoupon() == null) {
            throw new ResourceNotFoundException(
                    "Order has no coupon"
            );
        }

        order.setCoupon(null);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotal(order.getSubtotal());

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse pay(
            UUID orderId,
            PaymentRequest request
    ) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleViolationException(
                    "Only orders in PENDING_PAYMENT status can be paid"
            );
        }

        if (request.amount().compareTo(order.getTotal()) != 0) {
            throw new BusinessRuleViolationException(
                    "Payment amount must match the order total"
            );
        }

        Payment payment = new Payment(
                order,
                request.amount()
        );

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.DRAFT
                && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleViolationException(
                    "Only orders in DRAFT or PENDING_PAYMENT status can be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse ship(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessRuleViolationException(
                    "Only paid orders can be shipped"
            );
        }

        Shipment shipment = new Shipment(order);

        shipmentRepository.save(shipment);

        order.setStatus(OrderStatus.SHIPPED);

        return OrderResponse.from(order);
    }
}