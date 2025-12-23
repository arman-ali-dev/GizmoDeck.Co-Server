package com.example.ecommerce.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterResponse {
    private List<String> colors;
    private List<String> sizes;
    private List<String> brands;

    private int minPrice;
    private int maxPrice;

    private List<Integer> discounts;

    private Map<String, List<String>> specifications;
}
