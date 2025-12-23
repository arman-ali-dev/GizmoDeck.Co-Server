package com.example.ecommerce.repositories;

import com.example.ecommerce.models.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface VerificationRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
