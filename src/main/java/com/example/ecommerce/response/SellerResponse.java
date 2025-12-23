package com.example.ecommerce.response;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SellerResponse {

    private Long id;

    private String name;
    private String email;

    private String businessName;

    private String gstin;

    private LocalDateTime registeredOn;

    private boolean verified;
    private boolean active;

    private int totalOrders;
}
