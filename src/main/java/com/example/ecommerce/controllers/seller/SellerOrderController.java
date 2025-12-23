package com.example.ecommerce.controllers.seller;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Order;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.services.OrderService;
import com.example.ecommerce.services.SellerService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerOrderController(OrderService orderService, UserService userService, SellerRepository sellerRepository) {
        this.orderService = orderService;
        this.userService = userService;
        this.sellerRepository = sellerRepository;
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatusHandler(
            @PathVariable Long orderId,
            @RequestParam("status") OrderStatus orderStatus
    ) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrdersBySeller(@RequestHeader("Authorization") String jwt) {
        User loggedInUSer = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(loggedInUSer).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        List<Order> orders = orderService.getAllOrdersBySeller(seller.getId());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderBySeller(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws AccessDeniedException {

        User loggedInUSer = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(loggedInUSer).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        Order order = orderService.getOrderBySeller(seller.getId(), orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

}
