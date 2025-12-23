package com.example.ecommerce.requests;

import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {
    private Double ratingValue;
    private String comment;
    private List<String> images;
}
