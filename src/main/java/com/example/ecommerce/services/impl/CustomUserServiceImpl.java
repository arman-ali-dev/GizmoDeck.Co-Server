package com.example.ecommerce.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.ecommerce.enums.Role;
import com.example.ecommerce.exceptions.user.UserNotFoundException;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserServiceImpl implements UserDetailsService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    @Autowired
    public CustomUserServiceImpl(SellerRepository sellerRepository, UserRepository userRepository) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    private static final String SELLER_PREFIX = "seller_";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username);
        if (user != null) {
            return buildUserDetails(user.getEmail(), user.getRole().toString());
        }

        throw new UserNotFoundException("User not found with username: " + username);
    }

    private UserDetails buildUserDetails(String email, String role) {
        if (role == null) {
            role = Role.CUSTOMER.toString(); // default role
        }
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new org.springframework.security.core.userdetails.User(
                email,
                "", // password is empty since JWT/OTP is used
                authorities
        );
    }
}
