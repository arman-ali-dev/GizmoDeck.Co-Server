package com.example.ecommerce.controllers.admin;

import com.example.ecommerce.models.CouponCode;
import com.example.ecommerce.services.CouponCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponCodeController {
    private final CouponCodeService couponCodeService;

    @Autowired
    public AdminCouponCodeController(CouponCodeService couponCodeService) {
        this.couponCodeService = couponCodeService;
    }

    @PostMapping("/create")
    public ResponseEntity<CouponCode> createCoupon(@Valid @RequestBody CouponCode couponCode) throws AccessDeniedException {
        CouponCode createdCoupon = couponCodeService.createCouponCode(couponCode);
        return new ResponseEntity<>(createdCoupon, HttpStatus.CREATED);
    }


    @PutMapping("/update/{couponId}")
    public ResponseEntity<CouponCode> updateCoupon(@PathVariable Long couponId,
                                                   @RequestBody CouponCode couponCode) throws AccessDeniedException {
        CouponCode updatedCoupon = couponCodeService.updateCouponCode(couponId, couponCode);
        return new ResponseEntity<>(updatedCoupon, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{couponId}")
    public ResponseEntity<String> deleteCoupon(@PathVariable Long couponId) throws AccessDeniedException {
        couponCodeService.deleteCouponCode(couponId);
        return new ResponseEntity<>("Coupon deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CouponCode>> getAllCouponHandler() {
        List<CouponCode> allCouponCodes = couponCodeService.getAllCouponCodes();
        return new ResponseEntity<>(allCouponCodes, HttpStatus.OK);
    }
}
