package com.example.ecommerce.models;

import com.example.ecommerce.enums.AccountStatus;
import com.example.ecommerce.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Embedded
    private BusinessDetails businessDetails;

    @Embedded
    private BankDetails bankDetails;

    @OneToOne(cascade = CascadeType.ALL)
    private Address pickupAddress;

    private String GSTIN;
    private boolean isVerified = false;
    private boolean isActive= false;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;
}