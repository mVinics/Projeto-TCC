package com.br.tcc.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stock

) {
}