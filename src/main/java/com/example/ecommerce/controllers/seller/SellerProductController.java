package com.example.ecommerce.controllers.seller;

import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.services.ProductService;
import com.example.ecommerce.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private final ProductService productService;
    private final UserService userService;
    private final SellerRepository sellerRepository;


    @Autowired
    public SellerProductController(ProductService productService, UserService userService, SellerRepository sellerRepository) {
        this.productService = productService;
        this.userService = userService;
        this.sellerRepository = sellerRepository;
    }


    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            @RequestHeader("Authorization") String jwt) {
        User loggedInUSer = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(loggedInUSer).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        Product createdProduct = productService.createProduct(seller, product.getCategory().getId(), product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProductHandler(@PathVariable Long productId,
                                                        @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(productId, product);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }


    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProductHandler(@PathVariable Long productId) throws AccessDeniedException {
        productService.deleteProduct(productId);
        return new ResponseEntity<>("Product deleted successfully!", HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllSellerProducts(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(user).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        List<Product> products = productService.getProductsBySellerId(seller.getId());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

}
