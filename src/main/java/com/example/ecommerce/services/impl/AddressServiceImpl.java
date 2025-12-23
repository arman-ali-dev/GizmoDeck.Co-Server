package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.address.AddressNotFoundException;
import com.example.ecommerce.models.Address;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.AddressRepository;
import com.example.ecommerce.repositories.UserRepository;
import com.example.ecommerce.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Autowired
    public AddressServiceImpl(UserRepository userRepository,
                              AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }


    @Override
    public Address addAddress(Address address, User user) {

        address.setUser(user);

        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(Long addressId, Address address) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(
                        "Address not found with id: " + addressId));

        if (address.getName() != null && !address.getName().trim().isEmpty())
            existingAddress.setName(address.getName());

        if (address.getPhoneNumber() != null && !address.getPhoneNumber().trim().isEmpty())
            existingAddress.setPhoneNumber(address.getPhoneNumber());

        if (address.getPincode() != null && !address.getPincode().trim().isEmpty())
            existingAddress.setPincode(address.getPincode());

        if (address.getAddress() != null && !address.getAddress().trim().isEmpty())
            existingAddress.setAddress(address.getAddress());

        if (address.getLocality() != null && !address.getLocality().trim().isEmpty())
            existingAddress.setLocality(address.getLocality());

        if (address.getCity() != null && !address.getCity().trim().isEmpty())
            existingAddress.setCity(address.getCity());

        if (address.getState() != null && !address.getState().trim().isEmpty())
            existingAddress.setState(address.getState());


        return addressRepository.save(existingAddress);
    }

    @Override
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> new AddressNotFoundException("Address not found with id: " + addressId));

        addressRepository.delete(address);
    }

    @Override
    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(
                        () -> new AddressNotFoundException("Address not found with id: " + addressId));
    }
}
