package com.example.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "variants")
@Data
public class Variant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @ElementCollection
    private List<String> images;

    @NotNull
    private Long sellingPrice;

    @NotNull
    private Long mrpPrice;

    @NotNull
    private double discount;

    private Long stock;
    private Double weight;
    private String color;
    private String size;

    private boolean isActive = true;

    @ElementCollection
    private Map<String, String> specifications;

    @ElementCollection
    private List<String> keyFeatures;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
