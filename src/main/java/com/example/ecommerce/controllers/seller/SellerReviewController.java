package com.example.ecommerce.controllers.seller;


import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Review;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.services.ReviewService;
import com.example.ecommerce.services.SellerService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller/reviews")
public class SellerReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerReviewController(ReviewService reviewService, UserService userService, SellerRepository sellerRepository) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviewsForSeller(@RequestHeader("Authorization") String jwt) {
        User loggedInUSer = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(loggedInUSer).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        List<Review> reviews = reviewService.getAllReviewsForSeller(seller.getId());
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReviewBySeller(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt) throws AccessDeniedException {
        User loggedInUSer = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(loggedInUSer).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        reviewService.deleteReviewBySeller(seller.getId(), reviewId);
        return new ResponseEntity<>("Review deleted successfully!", HttpStatus.OK);
    }
}
