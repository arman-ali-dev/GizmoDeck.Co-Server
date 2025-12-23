package com.example.ecommerce.services.impl;

import com.example.ecommerce.enums.Role;
import com.example.ecommerce.exceptions.review.ReviewNotFoundException;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.Review;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.ReviewRepository;
import com.example.ecommerce.requests.CreateReviewRequest;
import com.example.ecommerce.services.ProductService;
import com.example.ecommerce.services.ReviewService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    final private ProductService productService;
    final private ReviewRepository reviewRepository;
    private final UserService userService;

    @Autowired
    public ReviewServiceImpl(ProductService productService, ReviewRepository reviewRepository, UserService userService) {
        this.productService = productService;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }

    @Override
    public Review createReview(User user, Long productId, CreateReviewRequest request) {
        Product product = productService.getProductById(productId);
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRatingValue(request.getRatingValue());
        review.setComment(request.getComment());


        if (!request.getImages().isEmpty()) {
            review.setImages(request.getImages());
        }

        return reviewRepository.save(review);
    }

    @Override
    public Review updateReview(Long reviewId, CreateReviewRequest request) throws AccessDeniedException {
        Review existingReview = this.getReviewById(reviewId);

        User currentUser = userService.getLoggedInUser();

        if (!existingReview.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to update this review");
        }

        if (request.getRatingValue() != null) {
            existingReview.setRatingValue(request.getRatingValue());
        }

        if (request.getComment() != null && !request.getComment().isEmpty()) {
            existingReview.setComment(request.getComment());
        }


        if (!request.getImages().isEmpty()) {
            existingReview.setImages(request.getImages());
        }

        return reviewRepository.save(existingReview);
    }


    @Override
    public List<Review> getProductReviews(Long productId) {
        productService.getProductById(productId);
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
    }

    @Override
    public void deleteReview(Long reviewId, User user) {
        Review review = this.getReviewById(reviewId);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this review");
        }
        reviewRepository.delete(review);
    }

    @Override
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }


    @Override
    public double getAverageRatingForProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            return 0.0; // No reviews → average 0
        }

        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRatingValue();
        }

        return sum / reviews.size();
    }

    @Override
    public List<Review> getAllReviewsForSeller(Long sellerId) {
        return reviewRepository.findAllByProduct_Seller_Id(sellerId);
    }

    @Override
    public void deleteReviewBySeller(Long sellerId, Long reviewId) throws AccessDeniedException {
        Review review = this.getReviewById(reviewId);

        Long reviewSellerId = review.getProduct().getSeller().getId();

        if (!reviewSellerId.equals(sellerId)) {
            throw new AccessDeniedException("You are not authorized to delete this review!");
        }

        reviewRepository.delete(review);
    }
}
