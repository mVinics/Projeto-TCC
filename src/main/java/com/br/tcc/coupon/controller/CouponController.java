package com.br.tcc.coupon.controller;

import com.br.tcc.coupon.dto.CouponResponse;
import com.br.tcc.coupon.dto.CreateCouponRequest;
import com.br.tcc.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        return couponService.create(request);
    }

    @GetMapping("/{code}")
    public CouponResponse getByCode(
            @PathVariable String code
    ) {
        return couponService.getByCode(code);
    }
}