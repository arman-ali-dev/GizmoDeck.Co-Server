package com.example.ecommerce.requests;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProductFilterRequest {
    private List<String> brands;
    private List<String> colors;
    private List<Double> discounts;
    private Map<String, List<String>> specifications;
    private Integer minPrice;
    private Integer maxPrice;
}
