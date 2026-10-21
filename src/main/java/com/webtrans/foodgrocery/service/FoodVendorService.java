package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.entity.FoodVendor;
import com.webtrans.foodgrocery.enums.VendorType;
import com.webtrans.foodgrocery.repository.FoodVendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodVendorService {

    private final FoodVendorRepository vendorRepository;

    public List<FoodVendor> getAllActiveVendors() {
        return vendorRepository.findByActiveTrue();
    }

    public List<FoodVendor> getVendorsByType(VendorType type) {
        return vendorRepository.findByTypeAndActiveTrue(type);
    }

    public Optional<FoodVendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    @Transactional
    public FoodVendor saveVendor(FoodVendor vendor) {
        return vendorRepository.save(vendor);
    }

    @Transactional
    public void deleteVendor(Long id) {
        FoodVendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setActive(false);
        vendorRepository.save(vendor);
    }

    @Transactional
    public void hardDeleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }

    public List<FoodVendor> searchVendors(String name) {
        return vendorRepository.findByNameContainingIgnoreCase(name);
    }

    public List<FoodVendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<FoodVendor> getVendorsByManagerId(Long managerId) {
        return vendorRepository.findByManagerId(managerId);
    }
}
