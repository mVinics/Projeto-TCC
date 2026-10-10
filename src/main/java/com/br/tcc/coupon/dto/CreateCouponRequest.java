package com.br.tcc.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCouponRequest(

        @NotBlank
        @Size(max = 50)
        String code,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal discountValue,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal minimumOrderValue,

        @NotNull
        LocalDate validFrom,

        @NotNull
        LocalDate validUntil

) {
}