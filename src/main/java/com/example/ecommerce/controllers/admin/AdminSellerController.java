package com.example.ecommerce.controllers.admin;

import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.response.SellerResponse;
import com.example.ecommerce.services.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sellers")
public class AdminSellerController {

    private final SellerService sellerService;

    @Autowired
    public AdminSellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<SellerResponse>> getAllSellersHandler() {
        List<SellerResponse> sellers = sellerService.getAllSellers();
        return new ResponseEntity<>(sellers, HttpStatus.OK);
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<Seller> getUserHandler(@PathVariable Long sellerId) {
        Seller seller = sellerService.getSellerById(sellerId);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @DeleteMapping("/{sellerId}")
    public ResponseEntity<String> deleteUserHandler(@PathVariable Long sellerId) {
        sellerService.deleteSeller(sellerId);
        return new ResponseEntity<>("Seller deleted successfully!", HttpStatus.OK);
    }


}
