package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.cart.CartNotFoundException;
import com.example.ecommerce.exceptions.couponCode.CouponCodeFoundException;
import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.CartItem;
import com.example.ecommerce.models.CouponCode;
import com.example.ecommerce.repositories.CartItemRepository;
import com.example.ecommerce.repositories.CartRepository;
import com.example.ecommerce.repositories.CouponCodeRepository;
import com.example.ecommerce.services.CouponCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CouponCodeServiceImpl implements CouponCodeService {

    private final CouponCodeRepository couponCodeRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CouponCodeServiceImpl(CouponCodeRepository couponCodeRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.couponCodeRepository = couponCodeRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CouponCode createCouponCode(CouponCode couponCode) throws AccessDeniedException {


        if (couponCode.getStartDate().isAfter(couponCode.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        if (couponCode.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0.");
        }

        if (couponCodeRepository.existsByCodeIgnoreCase(couponCode.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists.");
        }

        return couponCodeRepository.save(couponCode);
    }

    @Override
    public CouponCode updateCouponCode(Long couponCodeId, CouponCode updatedCoupon) throws AccessDeniedException {

        CouponCode existingCoupon = this.getCouponCodeById(couponCodeId);

        if (updatedCoupon.getStartDate() != null && updatedCoupon.getEndDate() != null &&
                updatedCoupon.getStartDate().isAfter(updatedCoupon.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        if (updatedCoupon.getCode() != null && !updatedCoupon.getCode().isEmpty()) {
            if (!existingCoupon.getCode().equalsIgnoreCase(updatedCoupon.getCode()) &&
                    couponCodeRepository.existsByCodeIgnoreCase(updatedCoupon.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists.");
            }
            existingCoupon.setCode(updatedCoupon.getCode());
        }

        if (updatedCoupon.getDiscountType() != null) {
            existingCoupon.setDiscountType(updatedCoupon.getDiscountType());
        }

        if (updatedCoupon.getDiscountValue() != null) {
            if (updatedCoupon.getDiscountValue() <= 0) {
                throw new IllegalArgumentException("Discount value must be greater than 0.");
            }
            existingCoupon.setDiscountValue(updatedCoupon.getDiscountValue());
        }

        if (updatedCoupon.getMinOrderAmount() != null) {
            existingCoupon.setMinOrderAmount(updatedCoupon.getMinOrderAmount());
        }

        if (updatedCoupon.getStartDate() != null) {
            existingCoupon.setStartDate(updatedCoupon.getStartDate());
        }

        if (updatedCoupon.getEndDate() != null) {
            existingCoupon.setEndDate(updatedCoupon.getEndDate());
        }

        if (updatedCoupon.getUsageLimit() != null) {
            existingCoupon.setUsageLimit(updatedCoupon.getUsageLimit());
        }

        existingCoupon.setActive(updatedCoupon.isActive());

        return couponCodeRepository.save(existingCoupon);
    }

    @Override
    public void deleteCouponCode(Long couponCodeId) throws AccessDeniedException {


        CouponCode existingCoupon = this.getCouponCodeById(couponCodeId);

        couponCodeRepository.delete(existingCoupon);
    }

    @Override
    public CouponCode getCouponCodeById(Long couponCodeId) {
        return couponCodeRepository.findById(couponCodeId)
                .orElseThrow(() -> new CouponCodeFoundException("Coupon code not found with id: " + couponCodeId));
    }

    @Override
    public CouponCode getCouponCodeByCode(String code) {
        CouponCode coupon = couponCodeRepository.findByCode(code);

        if (coupon == null) {
            throw new CouponCodeFoundException("Coupon code not found: " + code);
        }

        if (!coupon.isActive()) {
            throw new IllegalStateException("This coupon code is inactive or expired.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            throw new IllegalStateException("This coupon code is not valid at this time.");
        }

        return coupon;
    }

    @Override
    public List<CouponCode> getAllCouponCodes() {
        return couponCodeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Cart applyCouponCode(String code, Long userId) {

        CouponCode coupon = couponCodeRepository.findByCode(code);
        if (coupon == null) {
            throw new RuntimeException("Invalid coupon code");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() <= 0) {
            throw new RuntimeException("Coupon has reached its maximum usage limit!");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        if (cart.getCouponCode() != null) {
            throw new RuntimeException("A coupon is already applied to your cart.");
        }

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        long subTotal = cartItems.stream()
                .mapToLong(item -> item.getVariant().getMrpPrice() * item.getQuantity())
                .sum();

        long totalPrice = cartItems.stream()
                .mapToLong(item -> item.getVariant().getSellingPrice() * item.getQuantity())
                .sum();

        cart.setSubTotal(subTotal);
        cart.setTotalPrice(totalPrice);

        if (coupon.getMinOrderAmount() != null &&
                subTotal < coupon.getMinOrderAmount()) {
            throw new RuntimeException("Minimum order amount should be ₹" + coupon.getMinOrderAmount());
        }

        double discount = switch (coupon.getDiscountType()) {
            case PERCENTAGE -> (totalPrice * coupon.getDiscountValue()) / 100.0;
            case FIXED_AMOUNT -> coupon.getDiscountValue();
        };

        long finalAmount = Math.max(0, Math.round(totalPrice - discount));

        cart.setDiscount((long) discount);
        cart.setTotalPrice(finalAmount);

        // Assign coupon to cart
        cart.setCouponCode(coupon);

        if (coupon.getUsageLimit() != null) {
            coupon.setUsageLimit(coupon.getUsageLimit() - 1);
        }

        couponCodeRepository.save(coupon);
        return cartRepository.save(cart);
    }


}
