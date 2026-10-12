package com.br.tcc;

import com.br.tcc.order.entity.Order;
import com.br.tcc.order.entity.OrderStatus;
import com.br.tcc.product.entity.Product;
import com.br.tcc.product.entity.ProductStatus;
import com.br.tcc.product.repository.ProductRepository;
import com.br.tcc.coupon.entity.Coupon;
import com.br.tcc.coupon.entity.CouponStatus;
import com.br.tcc.coupon.repository.CouponRepository;
import com.br.tcc.TestcontainersConfiguration;
import com.br.tcc.customer.entity.Customer;
import com.br.tcc.customer.entity.CustomerStatus;
import com.br.tcc.customer.repository.CustomerRepository;
import com.br.tcc.order.entity.OrderItem;
import com.br.tcc.order.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clear();
    }

    private Order createDraftOrder() {
        Customer customer = customerRepository.save(
                new Customer(
                        "Test Customer",
                        "customer-" + UUID.randomUUID() + "@test.com",
                        CustomerStatus.ACTIVE
                )
        );

        return orderRepository.save(new Order(customer));
    }

    /**/

    private Order createDraftOrderWithItem(
            Product product,
            int quantity
    ) {
        Order order = createDraftOrder();

        OrderItem item = new OrderItem(
                order,
                product,
                quantity,
                product.getPrice()
        );

        order.addItem(item);

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotal(subtotal);

        return orderRepository.save(order);
    }

    /**/

    private Order createOrderWithStatus(OrderStatus status) {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        order.setStatus(status);

        return orderRepository.save(order);
    }

    /**/

    private Product createProduct(
            ProductStatus status,
            int stock
    ) {
        return productRepository.save(
                new Product(
                        "Test Product",
                        new BigDecimal("100.00"),
                        stock,
                        status
                )
        );
    }

    /**/

    private Coupon createCoupon(
            String code,
            CouponStatus status,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            LocalDate validFrom,
            LocalDate validUntil
    ) {
        return couponRepository.save(
                new Coupon(
                        code,
                        discountValue,
                        minimumOrderValue,
                        validFrom,
                        validUntil,
                        status
                )
        );
    }

    /**/

    //RN-001
    @Test
    void shouldCreateOrderForActiveCustomer() throws Exception {
        Customer customer = customerRepository.save(
                new Customer(
                        "Ana Silva",
                        "ana.order@example.com",
                        CustomerStatus.ACTIVE
                )
        );

        String request = """
                {
                  "customerId": "%s"
                }
                """.formatted(customer.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId")
                        .value(customer.getId().toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.subtotal").value(0))
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void shouldRejectOrderForBlockedCustomer() throws Exception {
        Customer customer = customerRepository.save(
                new Customer(
                        "Carlos Souza",
                        "carlos.blocked@example.com",
                        CustomerStatus.BLOCKED
                )
        );

        String request = """
                {
                  "customerId": "%s"
                }
                """.formatted(customer.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"));
    }

    //RN-002
    @Test
    void shouldAddActiveProductToOrder() throws Exception {
        Order order = createDraftOrder();

        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        String request = """
            {
              "productId": "%s",
              "quantity": 1
            }
            """.formatted(product.getId());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/items",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(product.getId().toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(1));
    }

    @Test
    void shouldRejectInactiveProduct() throws Exception {
        Order order = createDraftOrder();

        Product product = createProduct(
                ProductStatus.INACTIVE,
                10
        );

        String request = """
            {
              "productId": "%s",
              "quantity": 1
            }
            """.formatted(product.getId());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/items",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Only active products can be added to an order"));
    }

    //RN - 003
    @Test
    void shouldAddItemWithPositiveQuantity() throws Exception {
        Order order = createDraftOrder();

        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        String request = """
            {
              "productId": "%s",
              "quantity": 2
            }
            """.formatted(product.getId());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/items",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldRejectItemWithNonPositiveQuantity() throws Exception {
        Order order = createDraftOrder();

        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        String request = """
            {
              "productId": "%s",
              "quantity": 0
            }
            """.formatted(product.getId());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/items",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Item quantity must be greater than zero"));
    }

    //RN-004
    @Test
    void shouldAddItemWhenStockIsAvailable() throws Exception {
        Order order = createDraftOrder();

        Product product = createProduct(
                ProductStatus.ACTIVE,
                5
        );

        String request = """
            {
              "productId": "%s",
              "quantity": 5
            }
            """.formatted(product.getId());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/items",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.subtotal").value(500.00))
                .andExpect(jsonPath("$.total").value(500.00));
    }

    @Test
    void shouldRejectQuantityGreaterThanStock() throws Exception {
        Order order = createDraftOrder();

        Product product = createProduct(
                ProductStatus.ACTIVE,
                5
        );

        String request = """
            {
              "productId": "%s",
              "quantity": 6
            }
            """.formatted(product.getId());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/items",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Requested quantity exceeds available stock"));
    }

    //RN-005
    @Test
    void shouldModifyItemsWhileOrderIsDraft() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                1
        );

        String request = """
            {
              "quantity": 3
            }
            """;

        mockMvc.perform(patch(
                        "/api/v1/orders/{orderId}/items/{productId}",
                        order.getId(),
                        product.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.subtotal").value(300.00))
                .andExpect(jsonPath("$.total").value(300.00));
    }

    @Test
    void shouldRejectItemModificationAfterCheckout() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                1
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/checkout",
                        order.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PENDING_PAYMENT"));

        String request = """
            {
              "quantity": 2
            }
            """;

        mockMvc.perform(patch(
                        "/api/v1/orders/{orderId}/items/{productId}",
                        order.getId(),
                        product.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"));
    }

    //RN-006
    @Test
    void shouldCheckoutOrderWithAtLeastOneItem() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/checkout",
                        order.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PENDING_PAYMENT"));
    }

    @Test
    void shouldRejectCheckoutOfEmptyOrder() throws Exception {
        Order order = createDraftOrder();

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/checkout",
                        order.getId()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Order must have at least one item to checkout"));
    }

    //RN-007
    @Test
    void shouldCheckoutWhenStockRemainsAvailable() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                5
        );

        Order order = createDraftOrderWithItem(
                product,
                5
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/checkout",
                        order.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PENDING_PAYMENT"));
    }

    @Test
    void shouldRejectCheckoutWhenStockBecameInsufficient() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                8
        );

        product.setStock(5);
        productRepository.save(product);

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/checkout",
                        order.getId()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"));
    }

    //RN-008
    @Test
    void shouldApplyActiveCouponWithinValidityPeriod() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon coupon = createCoupon(
                "VALID20",
                CouponStatus.ACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        coupon.getCode()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("VALID20"))
                .andExpect(jsonPath("$.subtotal").value(200.00))
                .andExpect(jsonPath("$.discount").value(20.00))
                .andExpect(jsonPath("$.total").value(180.00));
    }

    @Test
    void shouldRejectExpiredOrInactiveCoupon() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon expiredCoupon = createCoupon(
                "EXPIRED",
                CouponStatus.ACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                today.minusDays(10),
                today.minusDays(1)
        );

        Coupon inactiveCoupon = createCoupon(
                "INACTIVE",
                CouponStatus.INACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        expiredCoupon.getCode()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Coupon must be active and within its validity period"));

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        inactiveCoupon.getCode()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Coupon must be active and within its validity period"));
    }

    //RN-009
    @Test
    void shouldApplyCouponWhenMinimumValueIsReached() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon coupon = createCoupon(
                "MINIMUM",
                CouponStatus.ACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("200.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        coupon.getCode()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(200.00))
                .andExpect(jsonPath("$.couponCode").value("MINIMUM"))
                .andExpect(jsonPath("$.discount").value(20.00))
                .andExpect(jsonPath("$.total").value(180.00));
    }

    @Test
    void shouldRejectCouponBelowMinimumOrderValue() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                1
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon coupon = createCoupon(
                "MIN300",
                CouponStatus.ACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("300.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        coupon.getCode()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Order does not meet the minimum value required by the coupon"));
    }

    //RN-010
    @Test
    void shouldAllowSingleCouponOnOrder() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon coupon = createCoupon(
                "SINGLE",
                CouponStatus.ACTIVE,
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        coupon.getCode()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("SINGLE"))
                .andExpect(jsonPath("$.discount").value(10.00));
    }
    @Test
    void shouldRejectSecondCouponOnSameOrder() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon firstCoupon = createCoupon(
                "FIRST",
                CouponStatus.ACTIVE,
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        Coupon secondCoupon = createCoupon(
                "SECOND",
                CouponStatus.ACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        firstCoupon.getCode()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("FIRST"));

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        secondCoupon.getCode()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Only one coupon can be applied to an order"));
    }

    //RN-011
    @Test
    void shouldChangeCouponWhileOrderIsDraft() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon firstCoupon = createCoupon(
                "OLD",
                CouponStatus.ACTIVE,
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        Coupon secondCoupon = createCoupon(
                "NEW",
                CouponStatus.ACTIVE,
                new BigDecimal("30.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        firstCoupon.getCode()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("OLD"));

        mockMvc.perform(delete(
                        "/api/v1/orders/{orderId}/coupon",
                        order.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").doesNotExist())
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.total").value(200.00));

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        secondCoupon.getCode()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("NEW"))
                .andExpect(jsonPath("$.discount").value(30.00))
                .andExpect(jsonPath("$.total").value(170.00));
    }

    @Test
    void shouldRejectCouponChangeAfterCheckout() throws Exception {
        Product product = createProduct(
                ProductStatus.ACTIVE,
                10
        );

        Order order = createDraftOrderWithItem(
                product,
                2
        );

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Coupon coupon = createCoupon(
                "CHECKOUT10",
                CouponStatus.ACTIVE,
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                today.minusDays(1),
                today.plusDays(1)
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/coupons/{code}",
                        order.getId(),
                        coupon.getCode()
                ))
                .andExpect(status().isOk());

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/checkout",
                        order.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PENDING_PAYMENT"));

        mockMvc.perform(delete(
                        "/api/v1/orders/{orderId}/coupon",
                        order.getId()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Coupon can only be removed while the order is in DRAFT status"));
    }

    //RN-012
    @Test
    void shouldPayPendingOrder() throws Exception {
        Order order = createOrderWithStatus(
                OrderStatus.PENDING_PAYMENT
        );

        String request = """
            {
              "amount": 200.00
            }
            """;

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/payment",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldRejectPaymentForOrderNotPendingPayment() throws Exception {
        Order order = createOrderWithStatus(
                OrderStatus.DRAFT
        );

        String request = """
            {
              "amount": 200.00
            }
            """;

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/payment",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Only orders in PENDING_PAYMENT status can be paid"));
    }

    //RN-013
    @Test
    void shouldAcceptPaymentMatchingOrderTotal() throws Exception {
        Order order = createOrderWithStatus(
                OrderStatus.PENDING_PAYMENT
        );

        String request = """
            {
              "amount": 200.00
            }
            """;

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/payment",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
    @Test
    void shouldRejectPaymentWithIncorrectAmount() throws Exception {
        Order order = createOrderWithStatus(
                OrderStatus.PENDING_PAYMENT
        );

        String request = """
            {
              "amount": 199.00
            }
            """;

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/payment",
                        order.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Payment amount must match the order total"));
    }

    //RN-014
    @Test
    void shouldCancelDraftOrPendingOrder() throws Exception {
        Order draftOrder = createOrderWithStatus(
                OrderStatus.DRAFT
        );

        Order pendingOrder = createOrderWithStatus(
                OrderStatus.PENDING_PAYMENT
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/cancel",
                        draftOrder.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/cancel",
                        pendingOrder.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));
    }
    @Test
    void shouldRejectCancellationOfPaidOrShippedOrder() throws Exception {
        Order paidOrder = createOrderWithStatus(
                OrderStatus.PAID
        );

        Order shippedOrder = createOrderWithStatus(
                OrderStatus.SHIPPED
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/cancel",
                        paidOrder.getId()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"));

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/cancel",
                        shippedOrder.getId()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"));
    }

    //RN-015
    @Test
    void shouldCreateShipmentForPaidOrder() throws Exception {
        Order order = createOrderWithStatus(
                OrderStatus.PAID
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/shipment",
                        order.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("SHIPPED"));
    }

    @Test
    void shouldRejectShipmentForUnpaidOrder() throws Exception {
        Order order = createOrderWithStatus(
                OrderStatus.PENDING_PAYMENT
        );

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/shipment",
                        order.getId()
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"))
                .andExpect(jsonPath("$.detail")
                        .value("Only paid orders can be shipped"));
    }

}