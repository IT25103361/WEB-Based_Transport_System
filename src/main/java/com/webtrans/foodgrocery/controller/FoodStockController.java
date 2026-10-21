package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.entity.FoodStock;
import com.webtrans.foodgrocery.service.FoodStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food/stock")
@RequiredArgsConstructor
public class FoodStockController {

    private final FoodStockService stockService;

    @GetMapping
    public ResponseEntity<List<FoodStock>> getAllStock() {
        return ResponseEntity.ok(stockService.getAllStock());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodStock> getStockById(@PathVariable Long id) {
        return stockService.getStockById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<FoodStock>> getStockByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockService.getStockByShop(shopId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<FoodStock>> getLowStockItems() {
        return ResponseEntity.ok(stockService.getLowStockItems());
    }

    @GetMapping("/shop/{shopId}/low-stock")
    public ResponseEntity<List<FoodStock>> getLowStockByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockService.getLowStockByShop(shopId));
    }

    @PostMapping
    public ResponseEntity<FoodStock> addStock(@RequestBody Map<String, Object> body) {
        Long productId = Long.parseLong(body.get("productId").toString());
        Long shopId = body.containsKey("shopId") && body.get("shopId") != null
                ? Long.parseLong(body.get("shopId").toString()) : null;
        Integer quantity = Integer.parseInt(body.get("quantity").toString());
        String unit = body.containsKey("unit") ? (String) body.get("unit") : null;
        Integer minLevel = body.containsKey("minStockLevel") && body.get("minStockLevel") != null
                ? Integer.parseInt(body.get("minStockLevel").toString()) : null;
        String notes = body.containsKey("notes") ? (String) body.get("notes") : null;
        return ResponseEntity.ok(stockService.addStock(productId, shopId, quantity, unit, minLevel, notes));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodStock> updateStock(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer quantity = body.containsKey("quantity") && body.get("quantity") != null
                ? Integer.parseInt(body.get("quantity").toString()) : null;
        Integer minLevel = body.containsKey("minStockLevel") && body.get("minStockLevel") != null
                ? Integer.parseInt(body.get("minStockLevel").toString()) : null;
        String unit = body.containsKey("unit") ? (String) body.get("unit") : null;
        String notes = body.containsKey("notes") ? (String) body.get("notes") : null;
        return ResponseEntity.ok(stockService.updateStock(id, quantity, minLevel, unit, notes));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<FoodStock> updateStockByProductId(@PathVariable Long productId, @RequestBody Map<String, Object> body) {
        Integer quantity = body.containsKey("quantity") && body.get("quantity") != null
                ? Integer.parseInt(body.get("quantity").toString()) : null;
        Integer minLevel = body.containsKey("minStockLevel") && body.get("minStockLevel") != null
                ? Integer.parseInt(body.get("minStockLevel").toString()) : null;
        String unit = body.containsKey("unit") ? (String) body.get("unit") : null;
        String notes = body.containsKey("notes") ? (String) body.get("notes") : null;
        return ResponseEntity.ok(stockService.updateStockByProductId(productId, quantity, minLevel, unit, notes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}
