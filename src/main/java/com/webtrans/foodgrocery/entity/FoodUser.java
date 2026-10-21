package com.webtrans.foodgrocery.entity;

import com.webtrans.foodgrocery.enums.Gender;
import com.webtrans.foodgrocery.enums.FoodRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "FoodUser")
@Table(name = "food_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String phoneNumber;

    private String address;

    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodRole role = FoodRole.CUSTOMER;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean active = true;

    /**
     * Optional explicit link to existing NEXTGO global account id.
     */
    @Column(name = "nextgo_user_id", nullable = true)
    private Long nextgoUserId;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
