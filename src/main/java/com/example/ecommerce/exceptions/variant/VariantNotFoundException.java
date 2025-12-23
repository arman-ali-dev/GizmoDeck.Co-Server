package com.example.ecommerce.exceptions.variant;

public class VariantNotFoundException extends RuntimeException {
    public VariantNotFoundException(String message) {
        super(message);
    }
}
