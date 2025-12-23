package com.example.ecommerce.response;

import com.example.ecommerce.models.Order;
import com.example.ecommerce.models.OrderItem;
import lombok.Data;

@Data
public class OrderDetailsResponse {
    private OrderItem orderItem;
    private Order order;
}
