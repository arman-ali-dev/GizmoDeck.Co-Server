package com.example.ecommerce.services.impl;

import com.example.ecommerce.config.JwtProvider;
import com.example.ecommerce.exceptions.user.UserNotFoundException;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.UserRepository;
import com.example.ecommerce.response.UserResponse;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    final private UserRepository userRepository;
    final private JwtProvider jwtProvider;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public User getUserProfile(String jwt) {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserNotFoundException("User not found with this email: " + email);
        }

        return user;
    }

    @Override
    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("No authenticated user found");
        }

        String username = authentication.getName();
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + username);
        }

        return user;
    }

    @Override
    public User updateProfile(User existingUser, User user) {
        System.out.println(user.getProfileImage());
        if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            existingUser.setFullName(user.getFullName());
        }

        if (user.getProfileImage() != null && !user.getProfileImage().trim().isEmpty()) {
            existingUser.setProfileImage(user.getProfileImage());
        }

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }

        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            existingUser.setEmail(user.getEmail());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(u -> {
            UserResponse res = new UserResponse();
            res.setId(u.getId());
            res.setFullName(u.getFullName());
            res.setEmail(u.getEmail());
            res.setCreatedAt(u.getCreatedAt());
            res.setTotalOrders(u.getOrders() != null ? u.getOrders().size() : 0);
            return res;
        }).collect(Collectors.toList());
    }


    @Override
    public void deleteUser(Long userId) {
        User user = this.getUserById(userId);
        userRepository.delete(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }


}
