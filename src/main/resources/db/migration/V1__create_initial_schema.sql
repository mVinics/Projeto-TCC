CREATE TABLE customers (
                           id UUID PRIMARY KEY,
                           name VARCHAR(150) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           status VARCHAR(30) NOT NULL
);

CREATE TABLE products (
                          id UUID PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          price NUMERIC(19, 2) NOT NULL,
                          stock INTEGER NOT NULL,
                          status VARCHAR(30) NOT NULL,

                          CONSTRAINT chk_products_price
                              CHECK (price >= 0),

                          CONSTRAINT chk_products_stock
                              CHECK (stock >= 0)
);

CREATE TABLE coupons (
                         id UUID PRIMARY KEY,
                         code VARCHAR(50) NOT NULL UNIQUE,
                         discount_value NUMERIC(19, 2) NOT NULL,
                         minimum_order_value NUMERIC(19, 2) NOT NULL,
                         valid_from DATE NOT NULL,
                         valid_until DATE NOT NULL,
                         status VARCHAR(30) NOT NULL,

                         CONSTRAINT chk_coupons_discount
                             CHECK (discount_value >= 0),

                         CONSTRAINT chk_coupons_minimum
                             CHECK (minimum_order_value >= 0),

                         CONSTRAINT chk_coupons_validity
                             CHECK (valid_until >= valid_from)
);

CREATE TABLE orders (
                        id UUID PRIMARY KEY,

                        customer_id UUID NOT NULL,
                        coupon_id UUID,

                        status VARCHAR(30) NOT NULL,

                        subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0,
                        discount NUMERIC(19, 2) NOT NULL DEFAULT 0,
                        total NUMERIC(19, 2) NOT NULL DEFAULT 0,

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES customers(id),

                        CONSTRAINT fk_orders_coupon
                            FOREIGN KEY (coupon_id)
                                REFERENCES coupons(id),

                        CONSTRAINT chk_orders_subtotal
                            CHECK (subtotal >= 0),

                        CONSTRAINT chk_orders_discount
                            CHECK (discount >= 0),

                        CONSTRAINT chk_orders_total
                            CHECK (total >= 0)
);

CREATE TABLE order_items (
                             order_id UUID NOT NULL,
                             product_id UUID NOT NULL,

                             quantity INTEGER NOT NULL,
                             unit_price NUMERIC(19, 2) NOT NULL,

                             PRIMARY KEY (order_id, product_id),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(id),

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_unit_price
                                 CHECK (unit_price >= 0)
);

CREATE TABLE payments (
                          id UUID PRIMARY KEY,
                          order_id UUID NOT NULL UNIQUE,

                          amount NUMERIC(19, 2) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          paid_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT fk_payments_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(id),

                          CONSTRAINT chk_payments_amount
                              CHECK (amount >= 0)
);

CREATE TABLE shipments (
                           id UUID PRIMARY KEY,
                           order_id UUID NOT NULL UNIQUE,

                           status VARCHAR(30) NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                           CONSTRAINT fk_shipments_order
                               FOREIGN KEY (order_id)
                                   REFERENCES orders(id)
);

/********************************************************************/

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);

CREATE INDEX idx_coupons_code
    ON coupons(code);