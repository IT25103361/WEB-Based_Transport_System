package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.entity.FoodProduct;
import com.webtrans.foodgrocery.entity.FoodShop;
import com.webtrans.foodgrocery.entity.FoodStock;
import com.webtrans.foodgrocery.repository.FoodProductRepository;
import com.webtrans.foodgrocery.repository.FoodShopRepository;
import com.webtrans.foodgrocery.repository.FoodStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodStockService {

    private final FoodStockRepository stockRepository;
    private final FoodProductRepository productRepository;
    private final FoodShopRepository shopRepository;

    public List<FoodStock> getAllStock() {
        return stockRepository.findAll();
    }

    public List<FoodStock> getStockByShop(Long shopId) {
        return stockRepository.findByShopId(shopId);
    }

    public Optional<FoodStock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    public Optional<FoodStock> getStockByProductAndShop(Long productId, Long shopId) {
        return stockRepository.findByProductIdAndShopId(productId, shopId);
    }

    public List<FoodStock> getLowStockItems() {
        return stockRepository.findLowStockItems();
    }

    public List<FoodStock> getLowStockByShop(Long shopId) {
        return stockRepository.findLowStockByShop(shopId);
    }

    @Transactional
    public FoodStock addStock(Long productId, Long shopId, Integer quantity, String unit, Integer minLevel, String notes) {
        FoodProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        FoodShop shop = shopId != null ? shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId)) : null;

        Optional<FoodStock> existing = shopId != null
                ? stockRepository.findByProductIdAndShopId(productId, shopId)
                : Optional.empty();

        if (existing.isPresent()) {
            FoodStock s = existing.get();
            s.setQuantity(s.getQuantity() + quantity);
            if (unit != null) s.setUnit(unit);
            if (minLevel != null) s.setMinStockLevel(minLevel);
            if (notes != null) s.setNotes(notes);
            FoodStock savedStock = stockRepository.save(s);

            product.setStockQuantity(s.getQuantity());
            productRepository.save(product);

            return savedStock;
        }

        FoodStock stock = new FoodStock();
        stock.setProduct(product);
        stock.setShop(shop);
        stock.setQuantity(quantity);
        stock.setUnit(unit);
        stock.setMinStockLevel(minLevel != null ? minLevel : 10);
        stock.setNotes(notes);
        FoodStock savedStock = stockRepository.save(stock);

        product.setStockQuantity(quantity);
        productRepository.save(product);

        return savedStock;
    }

    @Transactional
    public FoodStock updateStock(Long id, Integer quantity, Integer minLevel, String unit, String notes) {
        FoodStock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock record not found: " + id));
        if (quantity != null) {
            stock.setQuantity(quantity);
            FoodProduct product = stock.getProduct();
            if (product != null) {
                product.setStockQuantity(quantity);
                productRepository.save(product);
            }
        }
        if (minLevel != null) stock.setMinStockLevel(minLevel);
        if (unit != null) stock.setUnit(unit);
        if (notes != null) stock.setNotes(notes);
        return stockRepository.save(stock);
    }

    @Transactional
    public FoodStock updateStockByProductId(Long productId, Integer quantity, Integer minLevel, String unit, String notes) {
        List<FoodStock> stocks = stockRepository.findByProductId(productId);
        if (stocks.isEmpty()) {
            throw new RuntimeException("Stock record not found for product: " + productId);
        }
        FoodStock stock = stocks.get(0);
        if (quantity != null) {
            stock.setQuantity(quantity);
            FoodProduct product = stock.getProduct();
            if (product != null) {
                product.setStockQuantity(quantity);
                productRepository.save(product);
            }
        }
        if (minLevel != null) stock.setMinStockLevel(minLevel);
        if (unit != null) stock.setUnit(unit);
        if (notes != null) stock.setNotes(notes);
        return stockRepository.save(stock);
    }

    @Transactional
    public void deleteStock(Long id) {
        stockRepository.deleteById(id);
    }
}
