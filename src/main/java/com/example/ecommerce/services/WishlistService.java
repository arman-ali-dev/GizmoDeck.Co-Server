package com.example.ecommerce.services;

import com.example.ecommerce.models.User;
import com.example.ecommerce.models.Wishlist;
import com.example.ecommerce.models.WishlistItem;

import java.util.Optional;

public interface WishlistService {
    WishlistItem addItemToWishlist(User user, Long productId);

    void removeItemFromWishlist(Long wishlistItemId);

    Wishlist getWishlist(User user);

    boolean isProductInWishlist(User user, Long productId);
}