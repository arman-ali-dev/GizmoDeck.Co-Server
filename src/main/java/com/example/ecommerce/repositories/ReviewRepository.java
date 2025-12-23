package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);

    List<Review> findByProductId(Long productId);

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<Review> findAllByProduct_Seller_Id(Long sellerId);
}
