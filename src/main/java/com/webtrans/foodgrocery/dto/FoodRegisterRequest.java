package com.webtrans.foodgrocery.dto;

import com.webtrans.foodgrocery.enums.Gender;
import com.webtrans.foodgrocery.enums.FoodRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodRegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email/Username is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phoneNumber;

    private String address;

    private String postalCode;

    private Gender gender = Gender.OTHER;

    private FoodRole role = FoodRole.CUSTOMER;
}
