package com.webtrans.foodgrocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "FoodDeliveryPerson")
@Table(name = "food_delivery_persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodDeliveryPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private FoodUser user;

    private String name;

    private String phone;

    private String vehicleType;

    private boolean isAvailable = true;
}
