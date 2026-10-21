package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.entity.FoodProduct;
import com.webtrans.foodgrocery.entity.FoodShop;
import com.webtrans.foodgrocery.entity.FoodVendor;
import com.webtrans.foodgrocery.repository.FoodProductRepository;
import com.webtrans.foodgrocery.repository.FoodShopRepository;
import com.webtrans.foodgrocery.repository.FoodVendorRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodProductService {

    private final FoodProductRepository productRepository;
    private final FoodVendorRepository vendorRepository;
    private final FoodShopRepository shopRepository;

    public List<FoodProduct> getProductsByVendor(Long vendorId) {
        return productRepository.findByVendorIdAndAvailableTrue(vendorId);
    }

    public List<FoodProduct> getProductsByVendorAndCategory(Long vendorId, String category) {
        return productRepository.findByVendorIdAndCategory(vendorId, category);
    }

    public Optional<FoodProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public FoodProduct saveProduct(FoodProduct product) {
        return productRepository.save(product);
    }

    @Transactional
    public FoodProduct createProduct(Long vendorId, FoodProduct product) {
        if (vendorId != null) {
            FoodVendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            product.setVendor(vendor);
        }
        return productRepository.save(product);
    }

    @Transactional
    public FoodProduct createProductForShop(Long vendorId, Long shopId, FoodProduct product) {
        if (shopId != null) {
            FoodShop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new RuntimeException("Shop not found"));
            product.setShop(shop);
        }
        if (vendorId != null) {
            FoodVendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            product.setVendor(vendor);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        FoodProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAvailable(false);
        productRepository.save(product);
    }

    public List<FoodProduct> getPopularProducts() {
        return productRepository.findByPopularTrue();
    }

    public List<FoodProduct> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Page<FoodProduct> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<FoodProduct> getAllProducts() {
        return productRepository.findAll();
    }

    public List<FoodProduct> getProductsByShop(Long shopId) {
        return productRepository.findByShopIdAndAvailableTrue(shopId);
    }

    public List<FoodProduct> searchByCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }

    @Transactional
    public void hardDeleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Page<FoodProduct> advancedSearch(String query, String category, Long shopId, Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.findAll((Specification<FoodProduct>) (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (shopId != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}
