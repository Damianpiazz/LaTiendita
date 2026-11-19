package com.latiendita.offerService.coupon;

import com.latiendita.offerService.coupon.dto.CouponDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/{code}")
    public CouponDto findByCode(@PathVariable String code) {
        return couponService.validate(code);
    }
}
