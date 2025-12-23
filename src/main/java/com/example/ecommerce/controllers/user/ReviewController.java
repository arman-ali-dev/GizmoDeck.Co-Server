package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.Review;
import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.CreateReviewRequest;
import com.example.ecommerce.services.ReviewService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/product/reviews")
public class ReviewController {

    final private ReviewService reviewService;
    final private UserService userService;

    @Autowired
    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<Review> createReviewHandler(
            @PathVariable Long productId,
            @RequestBody CreateReviewRequest request,
            @RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        Review review = reviewService.createReview(user, productId, request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReviewHandler(@PathVariable Long reviewId,
                                                      @RequestBody CreateReviewRequest request) throws AccessDeniedException {
        Review review = reviewService.updateReview(reviewId, request);
        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    @GetMapping("/all/{productId}")
    public ResponseEntity<List<Review>> getProductReviewsHandler(@PathVariable Long productId) {
        List<Review> productReviews = reviewService.getProductReviews(productId);
        return new ResponseEntity<>(productReviews, HttpStatus.OK);
    }

    @GetMapping("/user/all/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserHandler(@PathVariable Long userId) {
        List<Review> reviewsByUser = reviewService.getReviewsByUser(userId);
        return new ResponseEntity<>(reviewsByUser, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserProfile(jwt);
        reviewService.deleteReview(reviewId, user);

        return ResponseEntity.ok("Review deleted successfully");
    }
}
