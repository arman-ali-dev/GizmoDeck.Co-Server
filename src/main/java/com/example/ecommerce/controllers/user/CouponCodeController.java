package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.CouponCode;
import com.example.ecommerce.models.User;
import com.example.ecommerce.services.CartService;
import com.example.ecommerce.services.CouponCodeService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponCodeController {


    private final CouponCodeService couponCodeService;
    private final UserService userService;

    @Autowired
    public CouponCodeController(CouponCodeService couponCodeService, UserService userService) {
        this.couponCodeService = couponCodeService;
        this.userService = userService;
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<CouponCode> getCouponById(@PathVariable Long couponId) {
        CouponCode coupon = couponCodeService.getCouponCodeById(couponId);
        return new ResponseEntity<>(coupon, HttpStatus.OK);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CouponCode> getCouponByCode(@PathVariable String code) {
        CouponCode coupon = couponCodeService.getCouponCodeByCode(code);
        return new ResponseEntity<>(coupon, HttpStatus.OK);
    }

    @PostMapping("/apply-coupon")
    public ResponseEntity<?> applyCoupon(
            @RequestParam String code,
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserProfile(jwt);
        Cart updatedCart = couponCodeService.applyCouponCode(code, user.getId());
        return ResponseEntity.ok(updatedCart);
    }
}
