package com.example.ecommerce.models;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BusinessDetails {
    private String businessName;
    private String businessEmail;
    private Long businessMobileNumber;
    private String businessAddress;
    private String businessLogo;
    private String banner;
}