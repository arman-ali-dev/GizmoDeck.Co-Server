package com.example.ecommerce.services.impl;

import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.User;
import com.example.ecommerce.models.Wishlist;
import com.example.ecommerce.models.WishlistItem;
import com.example.ecommerce.repositories.WishlistItemRepository;
import com.example.ecommerce.repositories.WishlistRepository;
import com.example.ecommerce.services.ProductService;
import com.example.ecommerce.services.WishlistService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductService productService;

    public WishlistServiceImpl(WishlistRepository wishlistRepository, WishlistItemRepository wishlistItemRepository, ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.productService = productService;
    }

    @Override
    public WishlistItem addItemToWishlist(User user, Long productId) {
        Product product = productService.getProductById(productId);

        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            // If no wishlist, create new
            Wishlist newWishlist = new Wishlist();
            newWishlist.setUser(user);
            return wishlistRepository.save(newWishlist);
        });

        Optional<WishlistItem> existingItem = wishlistItemRepository.findByWishlistAndProduct(wishlist, product);
        if (existingItem.isPresent()) {
            return existingItem.get();
        }

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProduct(product);

        return wishlistItemRepository.save(wishlistItem);
    }

    @Override
    public void removeItemFromWishlist(Long wishlistItemId) {
        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

        wishlistItemRepository.delete(item);
    }

    @Override
    public Wishlist getWishlist(User user) {
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    return wishlistRepository.save(newWishlist);
                });

        wishlist.getWishlistItems()
                .sort(Comparator.comparing(WishlistItem::getCreatedAt).reversed());

        return wishlist;
    }



    @Override
    public boolean isProductInWishlist(User user, Long productId) {
        Product product = productService.getProductById(productId);
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElse(null);

        if (wishlist == null) {
            return false;
        }

        return wishlistItemRepository.existsByWishlistAndProduct(wishlist, product);
    }
}
