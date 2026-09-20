package com.br.tcc.coupon.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumOrderValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponStatus status;

    protected Coupon() {
    }

    public Coupon(
            String code,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            LocalDate validFrom,
            LocalDate validUntil,
            CouponStatus status
    ) {
        this.code = code;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public void setMinimumOrderValue(BigDecimal minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public void setStatus(CouponStatus status) {
        this.status = status;
    }
}
