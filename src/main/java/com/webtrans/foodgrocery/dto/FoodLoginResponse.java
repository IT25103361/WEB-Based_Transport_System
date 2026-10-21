package com.webtrans.foodgrocery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodLoginResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String message;
    private boolean success;
}
