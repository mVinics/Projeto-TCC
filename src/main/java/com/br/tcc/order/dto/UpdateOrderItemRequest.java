package com.br.tcc.order.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderItemRequest(

        @NotNull
        Integer quantity

) {
}