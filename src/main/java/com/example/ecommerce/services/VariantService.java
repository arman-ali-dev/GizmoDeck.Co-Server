package com.example.ecommerce.services;

import com.example.ecommerce.models.Variant;

import java.util.List;

public interface VariantService {
    Variant createVariant(Long productId, Variant variant);

    void deleteVariant(Long variantId);

    Variant updateVariant(Long variantId, Variant variant);

    List<Variant> getProductVariants(Long productId);

    Variant getVariant(Long variantId);
}
