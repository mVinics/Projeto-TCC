package com.br.tcc.coupon.service;

import com.br.tcc.coupon.dto.CouponResponse;
import com.br.tcc.coupon.dto.CreateCouponRequest;
import com.br.tcc.coupon.entity.Coupon;
import com.br.tcc.coupon.entity.CouponStatus;
import com.br.tcc.coupon.repository.CouponRepository;
import com.br.tcc.shared.exception.ResourceConflictException;
import com.br.tcc.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CouponResponse create(CreateCouponRequest request) {
        if (couponRepository.existsByCode(request.code())) {
            throw new ResourceConflictException(
                    "A coupon with this code already exists"
            );
        }

        Coupon coupon = new Coupon(
                request.code(),
                request.discountValue(),
                request.minimumOrderValue(),
                request.validFrom(),
                request.validUntil(),
                CouponStatus.ACTIVE
        );

        Coupon savedCoupon = couponRepository.save(coupon);

        return CouponResponse.from(savedCoupon);
    }

    @Transactional(readOnly = true)
    public CouponResponse getByCode(String code) {
        Coupon coupon = findCouponByCode(code);

        return CouponResponse.from(coupon);
    }

    private Coupon findCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coupon not found: " + code
                ));
    }
}