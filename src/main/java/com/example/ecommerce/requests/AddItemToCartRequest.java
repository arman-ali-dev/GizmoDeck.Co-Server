package com.example.ecommerce.requests;

import lombok.Data;

@Data
public class AddItemToCartRequest {
    private int quantity;
    private Long price;
    private Long productId;
    private Long variantId;
}
