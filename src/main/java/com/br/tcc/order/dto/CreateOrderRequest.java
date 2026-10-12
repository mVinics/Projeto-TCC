package com.br.tcc.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(

        @NotNull
        UUID customerId

) {
}