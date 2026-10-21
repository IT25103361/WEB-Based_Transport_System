package com.webtrans.foodgrocery.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "FoodProduct")
@Table(name = "food_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Positive
    @Column(nullable = false)
    private Double price;

    private String imageUrl;

    private String category;

    private boolean available = true;

    private boolean popular = false;

    private Integer stockQuantity = 0;

    private String unit;

    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id")
    private FoodVendor vendor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id")
    private FoodShop shop;
}
