package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.entity.FoodVendor;
import com.webtrans.foodgrocery.enums.VendorType;
import com.webtrans.foodgrocery.service.FoodVendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food/vendors")
@RequiredArgsConstructor
public class FoodVendorController {

    private final FoodVendorService vendorService;

    @GetMapping
    public ResponseEntity<List<FoodVendor>> getAllVendors(
            @RequestParam(required = false) String type) {
        if (type != null) {
            VendorType vendorType = VendorType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(vendorService.getVendorsByType(vendorType));
        }
        return ResponseEntity.ok(vendorService.getAllActiveVendors());
    }

    @GetMapping("/all")
    public ResponseEntity<List<FoodVendor>> getAllVendorsAdmin() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodVendor> getVendorById(@PathVariable Long id) {
        return vendorService.getVendorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodVendor>> searchVendors(@RequestParam String name) {
        return ResponseEntity.ok(vendorService.searchVendors(name));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<FoodVendor>> getVendorsByManagerId(@PathVariable Long managerId) {
        return ResponseEntity.ok(vendorService.getVendorsByManagerId(managerId));
    }

    @PostMapping
    public ResponseEntity<FoodVendor> createVendor(@RequestBody FoodVendor vendor) {
        return ResponseEntity.ok(vendorService.saveVendor(vendor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodVendor> updateVendor(@PathVariable Long id, @RequestBody FoodVendor vendor) {
        vendor.setId(id);
        return ResponseEntity.ok(vendorService.saveVendor(vendor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteVendor(@PathVariable Long id) {
        vendorService.hardDeleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}
