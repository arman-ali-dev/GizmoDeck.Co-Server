package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerId(Long userId);

    Optional<Transaction> findByOrderId(Long orderId);

    List<Transaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.seller.id = :sellerId")
    Double findTotalAmountBySellerId(Long sellerId);

    @Query("""
       SELECT MONTH(t.createdAt) AS month, COALESCE(SUM(t.amount),0)
       FROM Transaction t
       WHERE t.seller.id = :sellerId
       GROUP BY MONTH(t.createdAt)
       ORDER BY MONTH(t.createdAt)
    """)
    List<Object[]> getMonthlyAmountBySellerId(Long sellerId);

}
