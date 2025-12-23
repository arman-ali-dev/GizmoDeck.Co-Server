package com.example.ecommerce.repositories;

import com.example.ecommerce.models.Address;
import com.example.ecommerce.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address,Long> {
    List<Address> findByUserOrderByCreatedAtDesc(User user);

}
