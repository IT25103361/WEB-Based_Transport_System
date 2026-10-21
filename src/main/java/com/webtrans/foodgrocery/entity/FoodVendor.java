package com.webtrans.foodgrocery.entity;

import com.webtrans.foodgrocery.enums.VendorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "FoodVendor")
@Table(name = "food_vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    private String address;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorType type = VendorType.FOOD; // FOOD or GROCERY

    private Double rating = 4.5;

    private Integer deliveryTimeMinutes = 30;

    private Double deliveryFee = 150.0;

    private Long managerId;

    private boolean active = true;
}
