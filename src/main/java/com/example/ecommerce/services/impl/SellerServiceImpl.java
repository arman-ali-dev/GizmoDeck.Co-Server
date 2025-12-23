package com.example.ecommerce.services.impl;

import com.example.ecommerce.config.JwtProvider;
import com.example.ecommerce.enums.AccountStatus;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.exceptions.authentication.WrongOtpException;
import com.example.ecommerce.exceptions.seller.SellerAlreadyExistsException;
import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Address;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.User;
import com.example.ecommerce.models.VerificationCode;
import com.example.ecommerce.repositories.*;
import com.example.ecommerce.response.SellerResponse;
import com.example.ecommerce.services.EmailService;
import com.example.ecommerce.services.SellerService;
import com.example.ecommerce.utils.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final AddressRepository addressRepository;
    private final VerificationRepository verificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;


    @Autowired
    public SellerServiceImpl(SellerRepository sellerRepository, AddressRepository addressRepository, VerificationRepository verificationRepository, EmailService emailService, UserRepository userRepository, OrderRepository orderRepository) {
        this.sellerRepository = sellerRepository;
        this.addressRepository = addressRepository;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Seller getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId).orElseThrow(() -> new SellerNotFoundException("Seller not found with id: " + sellerId));
    }



    public Seller createSeller(Seller sellerData, User currentUser) {
        // Check if already seller
        if (sellerRepository.findByUser(currentUser).isPresent()) {
            throw new SellerAlreadyExistsException("You are already a seller!");
        }

        // Save address
        Address pickupAddress = null;
        if (sellerData.getPickupAddress() != null) {
            pickupAddress = addressRepository.save(sellerData.getPickupAddress());
        }

        // Create new seller linked to user
        Seller seller = new Seller();
        seller.setUser(currentUser);
        seller.setBusinessDetails(sellerData.getBusinessDetails());
        seller.setBankDetails(sellerData.getBankDetails());
        seller.setPickupAddress(pickupAddress);
        seller.setGSTIN(sellerData.getGSTIN());
        seller.setVerified(false);
        seller.setActive(false);
        seller.setAccountStatus(AccountStatus.PENDING_VERIFICATION);

        Seller savedSeller = sellerRepository.save(seller);

        // Generate OTP
        String otp = OtpUtil.generateOtp();

        VerificationCode code = new VerificationCode();
        code.setOtp(otp);
        code.setEmail(currentUser.getEmail());
        code.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        verificationRepository.save(code);



        // Send email
        emailService.sendVerificationOtpEmail(currentUser.getEmail(), otp);

        return savedSeller;
    }

    private static Seller getSeller(Seller seller, Address pickupAddress) {
        Seller newSeller = new Seller();

//        newSeller.setName(seller.getName());
//        newSeller.setEmail(seller.getEmail());
//        newSeller.setPickupAddress(pickupAddress);
//        newSeller.setGSTIN(seller.getGSTIN());
//        newSeller.setPhoneNumber(seller.getPhoneNumber());
//        if (seller.getBusinessDetails() != null) {
//            newSeller.setBusinessDetails(seller.getBusinessDetails());
//        }
//        if (seller.getBankDetails() != null) {
//            newSeller.setBankDetails(seller.getBankDetails());
//        }
        return newSeller;
    }

    @Override
    public List<SellerResponse> getAllSellers() {
        List<Seller> sellers = sellerRepository.findAll();

        return sellers.stream().map(seller -> {
            int orderCount = orderRepository.countBySellerId(seller.getId());
            return mapToSellerResponse(seller, orderCount);
        }).collect(Collectors.toList());
    }


    private SellerResponse mapToSellerResponse(Seller seller, int orderCount) {
        SellerResponse response = new SellerResponse();

        response.setId(seller.getId());
        response.setName(seller.getUser().getFullName());
        response.setEmail(seller.getUser().getEmail());
        response.setBusinessName(seller.getBusinessDetails().getBusinessName());
        response.setGstin(seller.getGSTIN());
        response.setRegisteredOn(seller.getUser().getCreatedAt());
        response.setVerified(seller.isVerified());
        response.setActive(seller.isActive());
        response.setTotalOrders(orderCount);

        return response;
    }



    @Override
    public List<Seller> getSellersByAccountStatus(AccountStatus status) {
        return sellerRepository.findByAccountStatus(status);
    }

    @Override
    public Seller updateSeller(Long sellerId, Seller seller) {
        Seller existingSeller = getSellerById(sellerId);

        if (seller.getBusinessDetails() != null) {
            if (existingSeller.getBusinessDetails() == null) {
                existingSeller.setBusinessDetails(seller.getBusinessDetails());
            } else {
                if (seller.getBusinessDetails().getBusinessName() != null)
                    existingSeller.getBusinessDetails().setBusinessName(seller.getBusinessDetails().getBusinessName());
                if (seller.getBusinessDetails().getBusinessEmail() != null)
                    existingSeller.getBusinessDetails().setBusinessEmail(seller.getBusinessDetails().getBusinessEmail());
                if (seller.getBusinessDetails().getBusinessAddress() != null)
                    existingSeller.getBusinessDetails().setBusinessAddress(seller.getBusinessDetails().getBusinessAddress());
            }
        }

        if (seller.getBankDetails() != null) {
            if (existingSeller.getBankDetails() == null) {
                existingSeller.setBankDetails(seller.getBankDetails());
            } else {
                if (seller.getBankDetails().getAccountNumber() != null)
                    existingSeller.getBankDetails().setAccountNumber(seller.getBankDetails().getAccountNumber());
                if (seller.getBankDetails().getAccountHolderName() != null)
                    existingSeller.getBankDetails().setAccountHolderName(seller.getBankDetails().getAccountHolderName());
                if (seller.getBankDetails().getIfscCode() != null)
                    existingSeller.getBankDetails().setIfscCode(seller.getBankDetails().getIfscCode());
            }
        }

        if (seller.getPickupAddress() != null) existingSeller.setPickupAddress(seller.getPickupAddress());

        if (seller.getGSTIN() != null && !seller.getGSTIN().trim().isEmpty())
            existingSeller.setGSTIN(seller.getGSTIN());

        existingSeller.setVerified(seller.isVerified());
        existingSeller.setActive(seller.isActive());

        return sellerRepository.save(existingSeller);
    }

    @Override
    public void deleteSeller(Long sellerId) {
        Seller seller = this.getSellerById(sellerId);
        sellerRepository.delete(seller);
    }

    @Override
    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) {
        Seller seller = this.getSellerById(sellerId);
        seller.setAccountStatus(status);
        return sellerRepository.save(seller);
    }

    @Override
    public void verifySeller(User currentUser, String otp) {
        String email = currentUser.getEmail();

        VerificationCode code = verificationRepository.findByEmail(email);

        if (code == null || !code.getOtp().equals(otp)) {
            throw new WrongOtpException("Wrong Otp!");
        }


        Seller seller = sellerRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Seller not found!"));

        seller.setVerified(true);
        seller.setActive(true);
        seller.setAccountStatus(AccountStatus.ACTIVE);

        sellerRepository.save(seller);

        currentUser.setRole(Role.SELLER);
        userRepository.save(currentUser);


        verificationRepository.delete(code);
    }



}
