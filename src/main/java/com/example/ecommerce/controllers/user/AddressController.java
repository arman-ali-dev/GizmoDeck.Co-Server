package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.Address;
import com.example.ecommerce.models.User;
import com.example.ecommerce.services.AddressService;
import com.example.ecommerce.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {


    private final AddressService addressService;
    private final UserService userService;

    @Autowired
    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<Address> addAddressHandler(
            @RequestHeader("Authorization") String jwt, @Valid @RequestBody Address address) {
        User user = userService.getUserProfile(jwt);
        Address savedAddress = addressService.addAddress(address, user);

        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddressHandler(
            @PathVariable Long addressId,  @RequestBody Address address) {
        Address updatedAddress = addressService.updateAddress(addressId, address);

        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddressHandler(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);

        return new ResponseEntity<>("Address deleted successfully!", HttpStatus.OK);
    }

    @GetMapping("/all/user")
    public ResponseEntity<List<Address>> getUserAddressesHandler(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        List<Address> userAddresses = addressService.getUserAddresses(user);

        return new ResponseEntity<>(userAddresses, HttpStatus.OK);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddressHandler(@PathVariable Long addressId) {

        Address addressById = addressService.getAddressById(addressId);

        return new ResponseEntity<>(addressById, HttpStatus.OK);
    }
}
