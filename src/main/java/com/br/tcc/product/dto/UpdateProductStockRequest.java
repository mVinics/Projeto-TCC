package com.br.tcc.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateProductStockRequest(

        @NotNull
        @PositiveOrZero
        Integer stock

) {
}