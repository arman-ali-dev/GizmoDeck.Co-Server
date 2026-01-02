package com.example.ecommerce.models;

import jakarta.persistence.Entity;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankDetails {
    private Long accountNumber;
    private String accountHolderName;
    private String ifscCode;
}