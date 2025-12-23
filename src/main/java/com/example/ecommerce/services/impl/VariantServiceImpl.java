package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.variant.VariantNotFoundException;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.Variant;
import com.example.ecommerce.repositories.ProductRepository;
import com.example.ecommerce.repositories.VariantRepository;
import com.example.ecommerce.services.ProductService;
import com.example.ecommerce.services.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ProductService productService;

    @Autowired
    public VariantServiceImpl(VariantRepository variantRepository, ProductService productService, ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productService = productService;
    }

    @Override
    public Variant createVariant(Long productId, Variant variant) {
        Product product = productService.getProductById(productId);

        variant.setProduct(product);

        if (product.getVariants() != null) {
            product.getVariants().add(variant);
        }

        return variantRepository.save(variant);
    }

    @Override
    public void deleteVariant(Long variantId) {
        Variant variant = this.getVariant(variantId);
        variantRepository.delete(variant);
    }

    @Override
    public Variant updateVariant(Long variantId, Variant variant) {
        Variant existingVariant = this.getVariant(variantId);

        if (variant.getImages() != null) existingVariant.setImages(variant.getImages());
        if (variant.getSellingPrice() != null) existingVariant.setSellingPrice(variant.getSellingPrice());
        if (variant.getMrpPrice() != null) existingVariant.setMrpPrice(variant.getMrpPrice());
        if (variant.getStock() != null) existingVariant.setStock(variant.getStock());
        if (variant.getWeight() != null) existingVariant.setWeight(variant.getWeight());
        if (variant.getColor() != null) existingVariant.setColor(variant.getColor());
        if (variant.getSize() != null) existingVariant.setSize(variant.getSize());
        if (variant.isActive() != existingVariant.isActive()) existingVariant.setActive(variant.isActive());

        return variantRepository.save(existingVariant);
    }

    @Override
    public List<Variant> getProductVariants(Long productId) {
        return variantRepository.getVariantsByProductId(productId);
    }

    @Override
    public Variant getVariant(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with id: " + variantId));
    }
}
