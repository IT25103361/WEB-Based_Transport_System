package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.entity.FoodProduct;
import com.webtrans.foodgrocery.service.FoodProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food/products")
@RequiredArgsConstructor
public class FoodProductController {

    private final FoodProductService productService;

    @GetMapping
    public ResponseEntity<List<FoodProduct>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodProduct> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FoodProduct>> getPopularProducts() {
        return ResponseEntity.ok(productService.getPopularProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FoodProduct>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(productService.advancedSearch(
                query, category, shopId, minPrice, maxPrice,
                PageRequest.of(page, size, Sort.by(sortBy))));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<FoodProduct>> getProductsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getProductsByShop(shopId));
    }

    @PostMapping
    public ResponseEntity<FoodProduct> createProduct(@RequestBody FoodProduct product,
                                                 @RequestParam(required = false) Long shopId,
                                                 @RequestParam(required = false) Long vendorId) {
        return ResponseEntity.ok(productService.createProductForShop(vendorId, shopId, product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodProduct> updateProduct(@PathVariable Long id,
                                                 @RequestBody FoodProduct product,
                                                 @RequestParam(required = false) Long shopId,
                                                 @RequestParam(required = false) Long vendorId) {
        product.setId(id);
        return ResponseEntity.ok(productService.createProductForShop(vendorId, shopId, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
