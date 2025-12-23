package com.example.ecommerce.requests;

import lombok.Data;

@Data
public class DirectOrderRequest {
    private Long productId;
    private Long variantId;
    private Integer quantity;
    private Long addressId;
}
