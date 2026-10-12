package com.br.tcc.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amount

) {
}