package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> getVariantsByProductId(Long productId);
}
