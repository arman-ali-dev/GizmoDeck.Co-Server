package com.example.ecommerce.controllers.seller;

import com.example.ecommerce.models.Variant;
import com.example.ecommerce.services.VariantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/variants")
public class SellerVariantController {

    private final VariantService variantService;

    @Autowired
    public SellerVariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<Variant> createVariantHandler(@PathVariable Long productId,
                                                        @Valid @RequestBody Variant variant) {
        variantService.createVariant(productId, variant);
        return new ResponseEntity<>(variant, HttpStatus.CREATED);
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<String> deleteVariantHandler(@PathVariable Long variantId) {
        variantService.deleteVariant(variantId);
        return new ResponseEntity<>("Variant deleted successfully!", HttpStatus.OK);
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<Variant> updateVariantHandler(@PathVariable Long variantId,
                                                        @Valid @RequestBody Variant variant) {
        Variant updatedVariant = variantService.updateVariant(variantId, variant);
        return new ResponseEntity<>(updatedVariant, HttpStatus.OK);
    }

}
