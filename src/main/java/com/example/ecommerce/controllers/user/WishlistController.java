package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.User;
import com.example.ecommerce.models.Wishlist;
import com.example.ecommerce.models.WishlistItem;
import com.example.ecommerce.services.UserService;
import com.example.ecommerce.services.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    public WishlistController(WishlistService wishlistService, UserService userService) {
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistItem> addItemToWishlistHandler(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        WishlistItem wishlistItem = wishlistService.addItemToWishlist(user, productId);

        return new ResponseEntity<>(wishlistItem, HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{wishlistItemId}")
    public ResponseEntity<String> removeItemFromWishlistHandler(
            @PathVariable Long wishlistItemId) {
        wishlistService.removeItemFromWishlist(wishlistItemId);

        return new ResponseEntity<>("Item Removed From Wishlist Successfully!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Wishlist> removeItemFromWishlistHandler(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        Wishlist wishlist = wishlistService.getWishlist(user);

        return new ResponseEntity<>(wishlist, HttpStatus.OK);
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkProductInWishlistHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long productId) {
        User user = userService.getUserProfile(jwt);
        boolean productInWishlist = wishlistService.isProductInWishlist(user, productId);

        return new ResponseEntity<>(productInWishlist, HttpStatus.OK);
    }

}
