package com.example.ecommerce.controllers.seller;

import com.example.ecommerce.enums.AccountStatus;
import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.models.VerificationCode;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.repositories.VerificationRepository;
import com.example.ecommerce.requests.LoginRequest;
import com.example.ecommerce.services.SellerService;
import com.example.ecommerce.services.UserService;
import com.example.ecommerce.utils.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerService sellerService;
    private final UserService userService;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerController(SellerService sellerService,
                            UserService userService,
                            SellerRepository sellerRepository) {
        this.sellerService = sellerService;
        this.userService = userService;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<Seller> getSellerHandler(@PathVariable Long sellerId) {
        Seller seller = sellerService.getSellerById(sellerId);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerProfileHandler(@RequestHeader("Authorization") String jwt) {
        User loggedInUSer = userService.getUserProfile(jwt);
        Seller sellerProfile = sellerRepository.findByUser(loggedInUSer).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        return new ResponseEntity<>(sellerProfile, HttpStatus.OK);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> createSellerHandler(@RequestBody Seller sellerData,
                                                 @RequestHeader("Authorization") String jwt) {
        User currentUser = userService.getUserProfile(jwt);
        Seller createdSeller = sellerService.createSeller(sellerData, currentUser);

        return new ResponseEntity<>(Map.of("message", "OTP sent to your email for verification."), HttpStatus.CREATED);
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifySellerHandler(@RequestParam String otp,
                                                      @RequestHeader("Authorization") String jwt) {
        User currentUser = userService.getUserProfile(jwt);
        sellerService.verifySeller(currentUser, otp);
        return new ResponseEntity<>("Seller Verified Successfully!", HttpStatus.OK);
    }




    @PatchMapping("/{sellerId}/status/{accountStatus}")
    public ResponseEntity<Seller> updateSellerAccountStatusHandler(@PathVariable Long sellerId, @PathVariable AccountStatus accountStatus) {
        Seller seller = sellerService.updateSellerAccountStatus(sellerId, accountStatus);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @GetMapping("/status/{accountStatus}")
    public ResponseEntity<List<Seller>> getSellersByAccountStatus(@PathVariable AccountStatus accountStatus) {
        List<Seller> sellers = sellerService.getSellersByAccountStatus(accountStatus);
        return new ResponseEntity<>(sellers, HttpStatus.OK);
    }

    @PutMapping("/{sellerId}")
    public ResponseEntity<Seller> updateSellerHandler(@PathVariable Long sellerId, @RequestBody Seller seller) {
        Seller updatedSeller = sellerService.updateSeller(sellerId, seller);
        return new ResponseEntity<>(updatedSeller, HttpStatus.OK);
    }

    @DeleteMapping("/{sellerId}")
    public ResponseEntity<String> deleteSellerHandler(@PathVariable Long sellerId) {
        sellerService.deleteSeller(sellerId);
        return new ResponseEntity<>("Seller deleted successfully!", HttpStatus.OK);
    }
}
