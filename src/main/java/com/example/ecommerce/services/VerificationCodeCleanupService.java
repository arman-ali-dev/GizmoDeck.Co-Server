package com.example.ecommerce.services;

import com.example.ecommerce.repositories.VerificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationCodeCleanupService {

    @Autowired
    private VerificationRepository repo;

    @Scheduled(fixedRate = 120000) // every 2 minute
    @Transactional
    public void deleteExpired() {
        repo.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}