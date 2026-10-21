package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.entity.FoodShop;
import com.webtrans.foodgrocery.enums.VendorType;
import com.webtrans.foodgrocery.repository.FoodShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodShopService {

    private final FoodShopRepository shopRepository;

    public List<FoodShop> getAllActiveShops() {
        return shopRepository.findByActiveTrue();
    }

    public List<FoodShop> getAllShops() {
        return shopRepository.findAll();
    }

    public List<FoodShop> getShopsByType(VendorType type) {
        return shopRepository.findByTypeAndActiveTrue(type);
    }

    public Optional<FoodShop> getShopById(Long id) {
        return shopRepository.findById(id);
    }

    @Transactional
    public FoodShop createShop(FoodShop shop) {
        return shopRepository.save(shop);
    }

    @Transactional
    public FoodShop updateShop(Long id, FoodShop shopData) {
        FoodShop existing = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));
        existing.setName(shopData.getName());
        existing.setDescription(shopData.getDescription());
        existing.setAddress(shopData.getAddress());
        existing.setCity(shopData.getCity());
        existing.setPhone(shopData.getPhone());
        existing.setEmail(shopData.getEmail());
        existing.setType(shopData.getType());
        existing.setDeliveryTimeMinutes(shopData.getDeliveryTimeMinutes());
        existing.setDeliveryFee(shopData.getDeliveryFee());
        existing.setOpeningHours(shopData.getOpeningHours());
        if (shopData.getImageUrl() != null) {
            existing.setImageUrl(shopData.getImageUrl());
        }
        return shopRepository.save(existing);
    }

    @Transactional
    public void deactivateShop(Long id) {
        FoodShop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));
        shop.setActive(false);
        shopRepository.save(shop);
    }

    @Transactional
    public void deleteShop(Long id) {
        shopRepository.deleteById(id);
    }

    public List<FoodShop> searchShops(String name) {
        return shopRepository.findByNameContainingIgnoreCase(name);
    }

    public List<FoodShop> searchShopsByCity(String city) {
        return shopRepository.findByCityContainingIgnoreCase(city);
    }

    public List<FoodShop> getShopsByManager(Long managerId) {
        return shopRepository.findByManagerId(managerId);
    }
}
