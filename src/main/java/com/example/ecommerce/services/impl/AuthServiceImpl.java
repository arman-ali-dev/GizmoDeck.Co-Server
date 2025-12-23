package com.example.ecommerce.services.impl;

import com.example.ecommerce.config.JwtProvider;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.exceptions.authentication.WrongOtpException;
import com.example.ecommerce.exceptions.user.UserAlreadyExistsException;
import com.example.ecommerce.exceptions.user.UserNotFoundException;
import com.example.ecommerce.models.Cart;
import com.example.ecommerce.models.User;
import com.example.ecommerce.models.VerificationCode;
import com.example.ecommerce.repositories.UserRepository;
import com.example.ecommerce.repositories.VerificationRepository;
import com.example.ecommerce.requests.LoginRequest;
import com.example.ecommerce.requests.OtpRequest;
import com.example.ecommerce.requests.RegisterRequest;
import com.example.ecommerce.response.AuthResponse;
import com.example.ecommerce.services.AuthService;
import com.example.ecommerce.services.EmailService;
import com.example.ecommerce.utils.OtpUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    final private UserRepository userRepository;
    final private VerificationRepository verificationRepository;
    final private EmailService emailService;
    final private JwtProvider jwtProvider;
    final private CustomUserServiceImpl customUserService;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           VerificationRepository verificationRepository,
                           EmailService emailService,
                           JwtProvider jwtProvider,
                           CustomUserServiceImpl customUserService) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.jwtProvider = jwtProvider;
        this.customUserService = customUserService;
    }


    @Override
    public void sendOtp(OtpRequest request) {
        String SIGNIN_PREFIX = "signin_";
        String email = request.getEmail();
        System.out.println("email "  + email);

        User user = userRepository.findByEmail(email.substring(SIGNIN_PREFIX.length()));

        if (email.startsWith(SIGNIN_PREFIX)) {
            email = email.substring(SIGNIN_PREFIX.length());
            if (user == null) {
                throw new UserNotFoundException("User not found with email: " + email);
            }
        } else {
            if (user != null) {
                throw new UserAlreadyExistsException("User already exists with email: " + email);
            }
        }



        VerificationCode isExists = verificationRepository.findByEmail(email);

        if (isExists != null) {
            verificationRepository.delete(isExists);
        }

        String otp = OtpUtil.generateOtp();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(1));


        verificationRepository.save(verificationCode);


        emailService.sendVerificationOtpEmail(email, otp);

    }

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        VerificationCode verificationCode = verificationRepository.findByEmail(request.getEmail());

        if (verificationCode == null || !verificationCode.getOtp().equals(request.getOtp())) {
            throw new WrongOtpException("Wrong Otp!");
        }

        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already exists with this email: = " + request.getEmail());
        }

        if (userRepository.findByPhoneNumber(request.getPhoneNumber()) != null) {
            throw new UserAlreadyExistsException("User already exists with this phone number: = " + request.getPhoneNumber());
        }

        User user = new User(request.getFullName(), request.getPhoneNumber(), request.getEmail());
        user.setVarified(true);


        Cart cart = new Cart();

        cart.setUser(user);
        user.setCart(cart);


        userRepository.save(user);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Role.CUSTOMER.toString()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        verificationRepository.delete(verificationCode);

        return new AuthResponse(jwt, "User Registration successfully!", Role.CUSTOMER);
    }

    @Override
    @Transactional
    public AuthResponse loginUser(LoginRequest request) {
        Authentication authentication = this.authenticate(request.getEmail(), request.getOtp());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        String jwt = jwtProvider.generateToken(authentication);
        verificationRepository.deleteByEmail(request.getEmail());
        return new AuthResponse(jwt, "Login Successfully!", Role.valueOf(roleName));
    }

    Authentication authenticate(String email, String otp) {
        UserDetails userDetails = customUserService.loadUserByUsername(email);
        if (email.startsWith("seller_")) {
            email = email.substring(7);
        }

        if (userDetails == null) {
            throw new UserNotFoundException("User not found with this email: " + email);
        }

        VerificationCode verificationCode = verificationRepository.findByEmail(email);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new WrongOtpException("Wrong OTP!");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
