package com.example.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id" )
    @JsonIgnore
    private User user;

    @NotNull
    private String name;

    @NotNull
    @Pattern(regexp="\\d{10}", message="Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull
    private String pincode;

    @NotNull
    private String address;

    @NotNull
    private String locality;

    @NotNull
    private String city;

    @NotNull
    private String state;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
