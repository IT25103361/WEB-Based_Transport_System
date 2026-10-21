package com.webtrans.foodgrocery.entity;

import com.webtrans.foodgrocery.enums.VendorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "FoodShop")
@Table(name = "food_shops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String phone;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorType type = VendorType.FOOD; // FOOD or GROCERY

    private Double rating = 0.0;

    private Integer deliveryTimeMinutes = 30;

    private Double deliveryFee = 150.0;

    private String openingHours;

    private boolean active = true;

    private Long managerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
