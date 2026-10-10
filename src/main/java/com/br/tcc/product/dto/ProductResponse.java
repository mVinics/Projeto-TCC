package com.br.tcc.product.dto;

import com.br.tcc.product.entity.Product;
import com.br.tcc.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        Integer stock,
        ProductStatus status
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getStatus()
        );
    }
}