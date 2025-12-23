package com.example.ecommerce.services;

public interface EmailService {
    void sendVerificationOtpEmail(String toEmail, String otp);
}
