package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.cart.CartAlreadyExistsException;
import com.example.ecommerce.exceptions.cart.CartItemNotFoundException;
import com.example.ecommerce.exceptions.cart.CartNotFoundException;
import com.example.ecommerce.models.*;
import com.example.ecommerce.repositories.CartItemRepository;
import com.example.ecommerce.repositories.CartRepository;
import com.example.ecommerce.requests.AddItemToCartRequest;
import com.example.ecommerce.services.CartService;
import com.example.ecommerce.services.ProductService;
import com.example.ecommerce.services.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;
    private final VariantService variantService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                           ProductService productService, CartItemRepository cartItemRepository, VariantService variantService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.cartItemRepository = cartItemRepository;
        this.variantService = variantService;
    }

    @Override
    public Cart createCart(User user) {
        if (cartRepository.existsByUserId(user.getId())) {
            throw new CartAlreadyExistsException("Cart already exists with user id: " + user.getId());
        }

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        return cartRepository.save(cart);
    }

    @Override
    public CartItem addItemToCart(AddItemToCartRequest request, User user) {

        // 1. Fetch product & variant
        Product product = productService.getProductById(request.getProductId());
        Variant variant = variantService.getVariant(request.getVariantId());

        long price = variant.getSellingPrice();

        // 2. Get or create Cart
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> this.createCart(user));

        // 3. Find existing cart item for this variant (only 1 because unique pair)
        Optional<CartItem> optionalCartItem =
                cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId());

        CartItem cartItem;

        if (optionalCartItem.isPresent()) {

            // Existing item → update quantity
            cartItem = optionalCartItem.get();

            int newQty = cartItem.getQuantity() + request.getQuantity();
            cartItem.setQuantity(newQty);
            cartItem.setTotalPrice(newQty * price);

        } else {

            // New item → create new CartItem
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setVariant(variant);
            cartItem.setProduct(product);

            cartItem.setQuantity(request.getQuantity());
            cartItem.setTotalPrice(price * request.getQuantity());
            cartItem.setActive(true);
        }

        // 4. Save cart item
        CartItem savedItem = cartItemRepository.save(cartItem);

        long subTotal = cartItemRepository.findByCart(cart).stream()
                .mapToLong(item -> item.getVariant().getMrpPrice() * item.getQuantity())
                .sum();

        long totalPrice = cartItemRepository.findByCart(cart).stream()
                .mapToLong(item -> item.getVariant().getSellingPrice() * item.getQuantity())
                .sum();

        long discount = subTotal - totalPrice;


        cart.setSubTotal(subTotal);
        cart.setTotalPrice(totalPrice);
        cart.setDiscount(discount);
        cartRepository.save(cart);

        return savedItem;
    }


    @Override
    public void removeItemFromCart(Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + itemId));

        Cart cart = cartItem.getCart();

        cartItemRepository.delete(cartItem);

        List<CartItem> items = cartItemRepository.findByCart(cart);

        long subTotal = items.stream()
                .mapToLong(item -> item.getVariant().getMrpPrice() * item.getQuantity())
                .sum();

        long totalPrice = items.stream()
                .mapToLong(item -> item.getVariant().getSellingPrice() * item.getQuantity())
                .sum();

        long discount = subTotal - totalPrice;

        cart.setSubTotal(subTotal);
        cart.setTotalPrice(totalPrice);
        cart.setDiscount(discount);

        cartRepository.save(cart);
    }


    @Override
    public void deleteCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        cartItemRepository.deleteAll(cart.getCartItems());

        cartRepository.delete(cart);
    }


    @Override
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());

        cart.getCartItems().clear();
        cart.setTotalPrice(0L);
        cart.setDiscount(0L);
        cart.setSubTotal(0L);
        cart.setCouponCode(null);
        cartRepository.save(cart);
    }

    @Override
    public Cart getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        List<CartItem> sortedItems = cartItemRepository.findByCartOrderByUpdatedAtDesc(cart);
        cart.setCartItems(sortedItems);

        return cart;
    }

    @Override
    public CartItem updateCartItemQuantity(Long itemId, int quantity, User user) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

        // Authorization Check
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Item does not belong to this user");
        }

        // Validate Quantity*2
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Variant variant = cartItem.getVariant();

        if (variant.getStock() < quantity) {
            throw new RuntimeException("Only " + variant.getStock() + " items left in stock");
        }

        // Update item price
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(variant.getSellingPrice() * quantity);

        CartItem updatedItem = cartItemRepository.save(cartItem);

        // ---------------- Recalculate the Cart ----------------
        Cart cart = cartItem.getCart();

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        long subTotal = cartItems.stream()
                .mapToLong(item -> item.getVariant().getMrpPrice() * item.getQuantity())
                .sum();

        long totalPrice = cartItems.stream()
                .mapToLong(item -> item.getVariant().getSellingPrice() * item.getQuantity())
                .sum();

        long discount = subTotal - totalPrice; // Normal MRP discount (not coupon)

        // Set values
        cart.setSubTotal(subTotal);
        cart.setTotalPrice(totalPrice);
        cart.setDiscount(discount);

        cartRepository.save(cart);

        return updatedItem;
    }


}
