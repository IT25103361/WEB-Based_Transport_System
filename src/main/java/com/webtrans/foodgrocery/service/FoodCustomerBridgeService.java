package com.webtrans.foodgrocery.service;

import com.webtrans.auth.model.User;
import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.enums.FoodRole;
import com.webtrans.foodgrocery.enums.Gender;
import com.webtrans.foodgrocery.repository.FoodUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoodCustomerBridgeService {

    private final FoodUserRepository foodUserRepository;

    @Transactional
    public FoodUser bridgeCustomer(User loggedUser) {
        if (loggedUser == null) {
            return null;
        }

        // 1. Check by nextgoUserId
        return foodUserRepository.findByNextgoUserId(loggedUser.getId())
                .orElseGet(() -> {
                    // 2. Fallback check by email
                    String email = loggedUser.getEmail() != null ? loggedUser.getEmail().toLowerCase() : "";
                    return foodUserRepository.findByEmail(email)
                            .map(existing -> {
                                existing.setNextgoUserId(loggedUser.getId());
                                return foodUserRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                // 3. Provision customer profile linked to NEXTGO account
                                FoodUser newUser = new FoodUser();
                                newUser.setFullName(loggedUser.getFullName() != null ? loggedUser.getFullName() : "Customer");
                                newUser.setEmail(email.isEmpty() ? "customer_" + loggedUser.getId() + "@nextgo.local" : email);
                                newUser.setPassword(loggedUser.getPassword() != null ? loggedUser.getPassword() : "bridged_account");
                                newUser.setPhoneNumber("0770000000");
                                newUser.setAddress("Colombo, Sri Lanka");
                                newUser.setPostalCode("00100");
                                newUser.setGender(Gender.OTHER);
                                newUser.setRole(FoodRole.CUSTOMER);
                                newUser.setNextgoUserId(loggedUser.getId());
                                newUser.setActive(true);
                                return foodUserRepository.save(newUser);
                            });
                });
    }

    public FoodUser resolveCurrentUser(jakarta.servlet.http.HttpSession session) {
        if (session == null) return null;
        FoodUser foodUser = (FoodUser) session.getAttribute("foodUser");
        if (foodUser != null) {
            return foodUser;
        }
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null) {
            foodUser = bridgeCustomer(loggedUser);
            if (foodUser != null) {
                session.setAttribute("foodUser", foodUser);
                session.setAttribute("foodRole", foodUser.getRole().name());
            }
        }
        return foodUser;
    }
}
