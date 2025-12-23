package com.example.ecommerce.controllers.seller;

import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.response.SellerDashboardResponse;
import com.example.ecommerce.services.SellerDashboardService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/dashboard")
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;
    private final SellerRepository sellerRepository;
    private final UserService userService;

    @Autowired
    public SellerDashboardController(SellerDashboardService dashboardService,
                                     SellerRepository sellerRepository,
                                     UserService userService) {
        this.dashboardService = dashboardService;
        this.sellerRepository = sellerRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<SellerDashboardResponse> getDashboard(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(user).orElseThrow(() -> new SellerNotFoundException("Seller not found with id: " + user.getId()));
        return new ResponseEntity<>(dashboardService.getDashboard(seller.getId()),HttpStatus.OK);
    }
}
