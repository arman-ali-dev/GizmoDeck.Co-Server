package com.example.ecommerce.requests;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String otp;
}
