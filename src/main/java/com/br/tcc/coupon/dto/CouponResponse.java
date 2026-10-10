package com.br.tcc.coupon.dto;

import com.br.tcc.coupon.entity.Coupon;
import com.br.tcc.coupon.entity.CouponStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        BigDecimal discountValue,
        BigDecimal minimumOrderValue,
        LocalDate validFrom,
        LocalDate validUntil,
        CouponStatus status
) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderValue(),
                coupon.getValidFrom(),
                coupon.getValidUntil(),
                coupon.getStatus()
        );
    }
}