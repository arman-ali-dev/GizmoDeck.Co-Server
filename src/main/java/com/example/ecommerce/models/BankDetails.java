package com.example.ecommerce.models;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data

public class BankDetails {
    private Long accountNumber;
    private String accountHolderName;
    private String ifscCode;
}