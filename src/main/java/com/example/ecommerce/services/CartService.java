package com.example.ecommerce.services;

import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.CartItem;
import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.AddItemToCartRequest;

public interface CartService {
    Cart createCart(User user);

    CartItem addItemToCart(AddItemToCartRequest request, User user);

    void removeItemFromCart(Long itemId);

    void deleteCart(Long cartId);

    void clearCart(Long userId);

    Cart getCart(Long userId);

    CartItem updateCartItemQuantity(Long itemId, int quantity, User user);
}
