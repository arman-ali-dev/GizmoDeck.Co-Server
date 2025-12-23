package com.example.ecommerce.services;

import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.CouponCode;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

public interface CouponCodeService {
    CouponCode createCouponCode(CouponCode couponCode) throws AccessDeniedException;

    CouponCode updateCouponCode(Long couponCodeId, CouponCode couponCode) throws AccessDeniedException;

    void deleteCouponCode(Long couponCodeId) throws AccessDeniedException;

    CouponCode getCouponCodeById(Long couponCodeId);

    CouponCode getCouponCodeByCode(String code);

    List<CouponCode> getAllCouponCodes();

    Cart applyCouponCode(String code, Long userId);
}
