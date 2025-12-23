package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.User;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
class UserController {
    final private UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfileHandler(@RequestHeader("Authorization") String jwt) {
        User userProfile = userService.getUserProfile(jwt);
        return new ResponseEntity<>(userProfile, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUserProfileHandler(
            @RequestHeader("Authorization") String jwt
            , @RequestBody User user) {
        User existingUser = userService.getUserProfile(jwt);
        User updateProfile = userService.updateProfile(existingUser, user);

        return new ResponseEntity<>(updateProfile, HttpStatus.OK);
    }

}
