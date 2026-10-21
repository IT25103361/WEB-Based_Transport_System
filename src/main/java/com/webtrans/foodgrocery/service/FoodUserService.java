package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.dto.FoodAddUserRequest;
import com.webtrans.foodgrocery.dto.FoodLoginRequest;
import com.webtrans.foodgrocery.dto.FoodLoginResponse;
import com.webtrans.foodgrocery.dto.FoodRegisterRequest;
import com.webtrans.foodgrocery.entity.FoodDeliveryPerson;
import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.enums.FoodRole;
import com.webtrans.foodgrocery.enums.Gender;
import com.webtrans.foodgrocery.repository.FoodDeliveryPersonRepository;
import com.webtrans.foodgrocery.repository.FoodUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodUserService {

    private final FoodUserRepository foodUserRepository;
    private final FoodDeliveryPersonRepository deliveryPersonRepository;

    @Transactional
    public FoodUser register(FoodRegisterRequest req) {
        String email = req.getEmail() != null ? req.getEmail().toLowerCase().trim() : "";
        if (foodUserRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        FoodUser user = new FoodUser();
        user.setFullName(req.getFullName());
        user.setEmail(email);
        user.setPassword(req.getPassword());
        user.setPhoneNumber(req.getPhoneNumber() != null ? req.getPhoneNumber() : "");
        user.setAddress(req.getAddress() != null ? req.getAddress() : "");
        user.setPostalCode(req.getPostalCode() != null ? req.getPostalCode() : "");
        user.setGender(req.getGender() != null ? req.getGender() : Gender.OTHER);
        user.setRole(req.getRole() != null ? req.getRole() : FoodRole.CUSTOMER);
        FoodUser saved = foodUserRepository.save(user);

        if (saved.getRole() == FoodRole.DELIVERY_PARTNER) {
            FoodDeliveryPerson dp = new FoodDeliveryPerson();
            dp.setUser(saved);
            dp.setName(saved.getFullName());
            dp.setPhone(saved.getPhoneNumber());
            dp.setVehicleType("Bike");
            dp.setAvailable(true);
            deliveryPersonRepository.save(dp);
        }

        return saved;
    }

    @Transactional
    public FoodUser addUser(FoodAddUserRequest req) {
        String email = req.getEmail() != null ? req.getEmail().toLowerCase().trim() : "";
        if (foodUserRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        FoodUser user = new FoodUser();
        user.setFullName(req.getFullName());
        user.setEmail(email);
        user.setPassword(req.getPassword());
        user.setPhoneNumber(req.getPhoneNumber() != null ? req.getPhoneNumber() : "");
        user.setAddress(req.getAddress() != null ? req.getAddress() : "");
        user.setPostalCode(req.getPostalCode() != null ? req.getPostalCode() : "");
        user.setGender(req.getGender() != null ? req.getGender() : Gender.OTHER);
        user.setRole(req.getRole() != null ? req.getRole() : FoodRole.CUSTOMER);
        FoodUser saved = foodUserRepository.save(user);

        if (saved.getRole() == FoodRole.DELIVERY_PARTNER) {
            FoodDeliveryPerson dp = new FoodDeliveryPerson();
            dp.setUser(saved);
            dp.setName(saved.getFullName());
            dp.setPhone(saved.getPhoneNumber());
            dp.setVehicleType("Bike");
            dp.setAvailable(true);
            deliveryPersonRepository.save(dp);
        }

        return saved;
    }

    public FoodLoginResponse login(FoodLoginRequest req) {
        String email = req.getEmail() != null ? req.getEmail().toLowerCase().trim() : "";
        Optional<FoodUser> userOpt = foodUserRepository.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(req.getPassword())) {
            return new FoodLoginResponse(null, null, null, null, "Invalid email or password", false);
        }
        FoodUser user = userOpt.get();
        if (!user.isActive()) {
            return new FoodLoginResponse(null, null, null, null, "Account is deactivated", false);
        }
        return new FoodLoginResponse(user.getId(), user.getFullName(), user.getEmail(),
                user.getRole().name(), "Login successful", true);
    }

    public List<FoodUser> getAllUsers() {
        return foodUserRepository.findAll();
    }

    public List<FoodUser> getAllCustomers() {
        return foodUserRepository.findByRole(FoodRole.CUSTOMER);
    }

    public List<FoodUser> getAllDeliveryPartners() {
        return foodUserRepository.findByRole(FoodRole.DELIVERY_PARTNER);
    }

    public Optional<FoodUser> getUserById(Long id) {
        return foodUserRepository.findById(id);
    }

    @Transactional
    public FoodUser toggleUserStatus(Long id) {
        FoodUser user = foodUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        return foodUserRepository.save(user);
    }

    @Transactional
    public FoodUser updateUser(Long id, FoodAddUserRequest req) {
        FoodUser user = foodUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail().toLowerCase().trim());
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            user.setPassword(req.getPassword());
        }
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getRole() != null) user.setRole(req.getRole());
        return foodUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        foodUserRepository.deleteById(id);
    }
}
