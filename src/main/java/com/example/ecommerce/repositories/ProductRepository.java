package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id <> :productId")
    List<Product> findSimilarProducts(Long categoryId, Long productId);

    List<Product> findByIsFeaturedTrue();

    List<Product> findByIsBestSellerTrue();

    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);


    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategory_Name(String categoryName);

    @Query("""
                SELECT p FROM Product p
                JOIN p.variants v
                WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                GROUP BY p.id
                ORDER BY MIN(v.sellingPrice) ASC
            """)
    List<Product> searchProductsSorted(@Param("keyword") String keyword);

    @Query("""
                SELECT p FROM Product p
                JOIN p.variants v
                GROUP BY p.id
                ORDER BY MIN(v.sellingPrice) ASC
            """)
    List<Product> sortBySellingPriceAsc();

    @Query("""
                SELECT p FROM Product p
                JOIN p.variants v
                GROUP BY p.id
                ORDER BY MIN(v.sellingPrice) DESC
            """)
    List<Product> sortBySellingPriceDesc();

    int countBySellerId(Long sellerId);



}
