package com.example.ecommerce.repositories;

import com.example.ecommerce.models.CouponCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponCodeRepository extends JpaRepository<CouponCode, Long> {
    boolean existsByCodeIgnoreCase(String code);

    CouponCode findByCode(String code);

    List<CouponCode> findAllByOrderByCreatedAtDesc();
}
