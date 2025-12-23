package com.example.ecommerce.services;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.models.Order;
import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.CheckoutRequest;
import com.example.ecommerce.requests.DirectOrderRequest;
import com.example.ecommerce.response.StripeResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(CheckoutRequest request, User user);

    StripeResponse checkoutProducts(CheckoutRequest request, User user);

    void cancelOrder(Long orderId);

    List<Order> getOrdersByUser(Long userId);

    Order getOrderById(Long orderId);

    Order updateOrderStatus(Long orderId, OrderStatus orderStatus);

    List<Order> getAllOrdersBySeller(Long sellerId);

    Order getOrderBySeller(Long sellerId, Long orderId) throws AccessDeniedException;

    Order directOrder(DirectOrderRequest request, User user);

    StripeResponse directCheckout(DirectOrderRequest request, User user);

}
