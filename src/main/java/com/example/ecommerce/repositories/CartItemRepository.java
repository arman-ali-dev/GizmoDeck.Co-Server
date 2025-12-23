package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.CartItem;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);

    List<CartItem> findByCart(Cart cart);

    List<CartItem> findByCartOrderByUpdatedAtDesc(Cart cart);

    void deleteByCartId(Long cartId);
}

