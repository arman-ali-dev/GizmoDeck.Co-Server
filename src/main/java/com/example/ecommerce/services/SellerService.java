package com.example.ecommerce.services;

import com.example.ecommerce.enums.AccountStatus;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.LoginRequest;
import com.example.ecommerce.response.SellerResponse;

import java.util.List;

public interface SellerService {
    Seller getSellerById(Long sellerId);


    Seller createSeller(Seller sellerData, User currentUser);

    List<SellerResponse> getAllSellers();

    List<Seller> getSellersByAccountStatus(AccountStatus status);

    Seller updateSeller(Long sellerId, Seller seller);

    void deleteSeller(Long sellerId);

    Seller updateSellerAccountStatus(Long sellerId, AccountStatus status);

    void verifySeller(User currentUser, String otp);


}
