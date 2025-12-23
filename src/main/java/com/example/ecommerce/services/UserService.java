package com.example.ecommerce.services;

import com.example.ecommerce.models.User;
import com.example.ecommerce.response.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getUserProfile(String jwt);

    User getLoggedInUser();

    User updateProfile(User existingUser, User user);

    List<UserResponse> getAllUsers();

    void deleteUser(Long userId);

    User getUserById(Long userId);

}
