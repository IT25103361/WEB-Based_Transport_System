package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.entity.FoodShop;
import com.webtrans.foodgrocery.enums.VendorType;
import com.webtrans.foodgrocery.service.FoodShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food/shops")
@RequiredArgsConstructor
public class FoodShopController {

    private final FoodShopService shopService;

    @GetMapping
    public ResponseEntity<List<FoodShop>> getAllShops(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city) {
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(shopService.searchShops(search));
        }
        if (city != null && !city.isEmpty()) {
            return ResponseEntity.ok(shopService.searchShopsByCity(city));
        }
        if (type != null && !type.isEmpty()) {
            VendorType vendorType = VendorType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(shopService.getShopsByType(vendorType));
        }
        return ResponseEntity.ok(shopService.getAllActiveShops());
    }

    @GetMapping("/all")
    public ResponseEntity<List<FoodShop>> getAllShopsAdmin() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodShop> getShopById(@PathVariable Long id) {
        return shopService.getShopById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<FoodShop>> getShopsByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(shopService.getShopsByManager(managerId));
    }

    @PostMapping
    public ResponseEntity<FoodShop> createShop(@RequestBody FoodShop shop) {
        return ResponseEntity.ok(shopService.createShop(shop));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodShop> updateShop(@PathVariable Long id, @RequestBody FoodShop shop) {
        return ResponseEntity.ok(shopService.updateShop(id, shop));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateShop(@PathVariable Long id) {
        shopService.deactivateShop(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
