package com.example.ecommerce.controllers.user;

import com.example.ecommerce.requests.LoginRequest;
import com.example.ecommerce.requests.OtpRequest;
import com.example.ecommerce.requests.RegisterRequest;
import com.example.ecommerce.response.ApiResponse;
import com.example.ecommerce.response.AuthResponse;
import com.example.ecommerce.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtpHandler(@RequestBody OtpRequest request) {
        authService.sendOtp(request);
        ApiResponse response = new ApiResponse("Otp Sent!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUserHandler(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUserHandler(@RequestBody LoginRequest request) throws Exception {
        AuthResponse response = authService.loginUser(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
