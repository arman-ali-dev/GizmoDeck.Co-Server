package com.example.ecommerce.repositories;

import com.example.ecommerce.models.User;
import com.example.ecommerce.models.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser(User user);
}
