package com.example.ecommerce.exceptions.authentication;


public class WrongOtpException extends RuntimeException {
    public WrongOtpException(String message) {
        super(message);
    }
}