package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.Product;
import com.example.ecommerce.requests.ProductFilterRequest;
import com.example.ecommerce.response.FilterResponse;
import com.example.ecommerce.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProductsHandler() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/all/featured")
    public ResponseEntity<List<Product>> getAllFeaturedProductsHandler() {
        List<Product> products = productService.getFeaturedProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/all/best-seller")
    public ResponseEntity<List<Product>> getAllBestSellerProductsHandler() {
        List<Product> products = productService.getBestSellerProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<List<Product>> getSellerProductsHandler(@PathVariable Long sellerId) {
        List<Product> products = productService.getProductsBySellerId(sellerId);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<List<Product>> getCategoryProductsHandler(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryId(categoryId);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/categories/by-name/{categoryName}")
    public ResponseEntity<List<Product>> getCategoryProductsByNameHandler(
            @PathVariable String categoryName) {
        List<Product> products = productService.getProductsByCategoryName(categoryName);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sort")
    public ResponseEntity<List<Product>> sortProductsHandler(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {
        List<Product> products = productService.sortProducts(sortBy, ascending);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsHandler(
            @RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductHandler(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<Product>> getSimilarProducts(@PathVariable Long id) {
        List<Product> similarProducts = productService.getSimilarProducts(id);
        return new ResponseEntity<>(similarProducts, HttpStatus.OK);
    }

    @GetMapping("/filters")
    public ResponseEntity<FilterResponse> getFilters(
            @RequestParam Long categoryId
    ) {
        FilterResponse filters = productService.getFilters(categoryId);
        return new ResponseEntity<>(filters, HttpStatus.OK);
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Product>> getFilteredProducts(
            @RequestBody ProductFilterRequest filterRequest
    ) {
        List<Product> products = productService.getFilteredProducts(filterRequest);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

}
