package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.Wishlist;
import com.example.ecommerce.models.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByWishlistAndProduct(Wishlist wishlist, Product product);

    boolean existsByWishlistAndProduct(Wishlist wishlist, Product product);
}
