package com.example.ecommerce.services;

import com.example.ecommerce.response.SellerDashboardResponse;

public interface SellerDashboardService {
    public SellerDashboardResponse getDashboard(Long sellerId);
}
