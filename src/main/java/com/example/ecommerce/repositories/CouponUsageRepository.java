package com.example.ecommerce.repositories;

import com.example.ecommerce.models.CouponCode;
import com.example.ecommerce.models.CouponUsage;
import com.example.ecommerce.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    boolean existsByUserAndCoupon(User user, CouponCode coupon);
}
