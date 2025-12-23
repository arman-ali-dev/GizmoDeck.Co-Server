package com.example.ecommerce.controllers.user;

import com.example.ecommerce.exceptions.order.OrderNotFoundException;
import com.example.ecommerce.models.Order;
import com.example.ecommerce.models.OrderItem;
import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.CheckoutRequest;
import com.example.ecommerce.requests.DirectOrderRequest;
import com.example.ecommerce.response.OrderDetailsResponse;
import com.example.ecommerce.response.StripeResponse;
import com.example.ecommerce.services.OrderService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkoutOrderHandler(@RequestBody CheckoutRequest request,
                                                               @RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        StripeResponse response = orderService.checkoutProducts(request, user);
        System.out.println(response.getMessage());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrderHandler(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>("Order cancelled successfully.", HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getOrdersByUserHandler(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        List<Order> orders = orderService.getOrdersByUser(user.getId());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{orderId}/{orderItemId}")
    public ResponseEntity<OrderDetailsResponse> getOrderItemHandler(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId
    ) {
        Order order = orderService.getOrderById(orderId);

        OrderItem item = order.getOrderItems()
                .stream()
                .filter(oi -> oi.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException("Order item not found!"));

        OrderDetailsResponse response = new OrderDetailsResponse();
        response.setOrder(order);
        response.setOrderItem(item);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/direct")
    public ResponseEntity<?> createDirectOrder(
            @RequestBody DirectOrderRequest req,
            @RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        StripeResponse response = orderService.directCheckout(req, user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
