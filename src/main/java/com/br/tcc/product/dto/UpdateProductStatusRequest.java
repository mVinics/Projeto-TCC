package com.br.tcc.product.dto;

import com.br.tcc.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(

        @NotNull
        ProductStatus status

) {
}