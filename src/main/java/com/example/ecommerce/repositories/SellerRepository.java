package com.example.ecommerce.repositories;

import com.example.ecommerce.enums.AccountStatus;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    List<Seller> findByAccountStatus(AccountStatus status);

    Optional<Seller> findByUser(User user);
}
