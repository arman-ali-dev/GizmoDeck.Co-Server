package com.example.ecommerce.services.impl;

import com.example.ecommerce.models.Order;
import com.example.ecommerce.repositories.OrderRepository;
import com.example.ecommerce.repositories.ProductRepository;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.repositories.TransactionRepository;
import com.example.ecommerce.response.MonthlyRevenueResponse;
import com.example.ecommerce.response.SellerDashboardResponse;
import com.example.ecommerce.services.SellerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public SellerDashboardServiceImpl(ProductRepository productRepository,
                                      OrderRepository orderRepository,
                                      TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
    }


    public SellerDashboardResponse getDashboard(Long sellerId) {

        int productCount = productRepository.countBySellerId(sellerId);
        int orderCount = orderRepository.countBySellerId(sellerId);

        // Prefer transaction sum if transactions exist
        Double revenue = transactionRepository.findTotalAmountBySellerId(sellerId);
        if (revenue == null) {
            // fallback to summing order.finalAmount (handle Number)
            Number total = orderRepository.findTotalRevenueBySellerId(sellerId);
            revenue = total == null ? 0.0 : total.doubleValue();
        }

        // Monthly graph: first try transactions
        List<Object[]> monthlyData = transactionRepository.getMonthlyAmountBySellerId(sellerId);
        if (monthlyData == null || monthlyData.isEmpty()) {
            monthlyData = orderRepository.getMonthlyRevenue(sellerId);
        }

        List<MonthlyRevenueResponse> revenueGraph = new ArrayList<>();
        for (Object[] row : monthlyData) {
            Number monthNum = (Number) row[0];
            Number amtNum = (Number) row[1];
            int month = monthNum == null ? 0 : monthNum.intValue();
            double amount = amtNum == null ? 0.0 : amtNum.doubleValue();

            // safety: month between 1..12
            String monthName = (month >= 1 && month <= 12) ? Month.of(month).name() : "UNKNOWN";
            revenueGraph.add(new MonthlyRevenueResponse(monthName, amount));
        }

        List<Order> recentOrders = orderRepository.findTop5BySellerIdOrderByCreatedAtDesc(sellerId);

        return new SellerDashboardResponse(
                productCount,
                orderCount,
                revenue,
                revenueGraph,
                recentOrders
        );
    }
}

