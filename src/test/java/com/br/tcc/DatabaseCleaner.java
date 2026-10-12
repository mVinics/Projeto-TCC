package com.br.tcc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void clear() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    payments,
                    shipments,
                    order_items,
                    orders,
                    coupons,
                    products,
                    customers
                CASCADE
                """);
    }
}