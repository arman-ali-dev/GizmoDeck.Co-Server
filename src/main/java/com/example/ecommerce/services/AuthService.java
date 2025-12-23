package com.example.ecommerce.services;

import com.example.ecommerce.models.User;
import com.example.ecommerce.requests.LoginRequest;
import com.example.ecommerce.requests.OtpRequest;
import com.example.ecommerce.requests.RegisterRequest;
import com.example.ecommerce.response.AuthResponse;

public interface AuthService {
    void sendOtp(OtpRequest request);

    AuthResponse registerUser(RegisterRequest request);

    AuthResponse loginUser(LoginRequest  request) throws Exception;
}
