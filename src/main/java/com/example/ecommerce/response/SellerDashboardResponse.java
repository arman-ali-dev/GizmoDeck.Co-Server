package com.example.ecommerce.response;

import com.example.ecommerce.models.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerDashboardResponse {

    private int totalProducts;
    private int totalOrders;
    private Double totalRevenue;

    private List<MonthlyRevenueResponse> revenueGraph;

    private List<Order> recentOrders;
}
