package com.example.ecommerce.repositories;

import com.example.ecommerce.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber);
}
