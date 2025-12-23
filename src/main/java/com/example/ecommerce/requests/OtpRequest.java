package com.example.ecommerce.requests;

import com.example.ecommerce.enums.Role;
import lombok.Data;

@Data
public class OtpRequest {
    private String email;
    private Role role;
}
