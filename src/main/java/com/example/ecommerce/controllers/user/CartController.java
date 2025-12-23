package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.CartItem;
import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.AddItemToCartRequest;
import com.example.ecommerce.services.CartService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    final private CartService cartService;
    final private UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/items/add")
    public ResponseEntity<CartItem> addItemToCartHandler(
            @RequestBody AddItemToCartRequest request,
            @RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        CartItem cartItem = cartService.addItemToCart(request, user);
        return new ResponseEntity<>(cartItem, HttpStatus.CREATED);
    }

    @DeleteMapping("/items/remove/{itemId}")
    public ResponseEntity<String> removeItemFromCartHandler(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return new ResponseEntity<>("Item removed successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCartHandler(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return new ResponseEntity<>("Cart deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<String> clearCartHandler(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        cartService.clearCart(user.getId());
        return new ResponseEntity<>("Cart cleared successfully", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Cart> getCartHandler(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        Cart cart = cartService.getCart(user.getId());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/item/{itemId}/quantity")
    public ResponseEntity<CartItem> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserProfile(jwt);
        CartItem updatedItem = cartService.updateCartItemQuantity(itemId, quantity, user);
        return new ResponseEntity<>(updatedItem, HttpStatus.OK);
    }

}
