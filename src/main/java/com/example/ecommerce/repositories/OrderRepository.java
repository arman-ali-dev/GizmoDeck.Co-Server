package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    int countBySellerId(Long sellerId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.seller.id = :sellerId")
    Number findTotalRevenueBySellerId(Long sellerId);
    @Query("""
       SELECT MONTH(o.createdAt) AS month, COALESCE(SUM(o.finalAmount),0)
       FROM Order o
       WHERE o.seller.id = :sellerId
       GROUP BY MONTH(o.createdAt)
       ORDER BY MONTH(o.createdAt)
    """)
    List<Object[]> getMonthlyRevenue(Long sellerId);

    List<Order> findTop5BySellerIdOrderByCreatedAtDesc(Long sellerId);
}
