package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
