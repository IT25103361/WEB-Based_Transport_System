package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.dto.FoodAddUserRequest;
import com.webtrans.foodgrocery.dto.FoodLoginRequest;
import com.webtrans.foodgrocery.dto.FoodLoginResponse;
import com.webtrans.foodgrocery.dto.FoodRegisterRequest;
import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.service.FoodCustomerBridgeService;
import com.webtrans.foodgrocery.service.FoodUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food/auth")
@RequiredArgsConstructor
public class FoodAuthController {

    private final FoodUserService userService;
    private final FoodCustomerBridgeService bridgeService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody FoodRegisterRequest req, HttpSession session) {
        try {
            FoodUser user = userService.register(req);
            session.setAttribute("foodUser", user);
            session.setAttribute("foodRole", user.getRole().name());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registration successful",
                    "userId", user.getId(),
                    "name", user.getFullName(),
                    "role", user.getRole().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@Valid @RequestBody FoodAddUserRequest req) {
        try {
            FoodUser user = userService.addUser(req);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User created successfully",
                    "userId", user.getId(),
                    "name", user.getFullName(),
                    "role", user.getRole().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<FoodLoginResponse> login(@Valid @RequestBody FoodLoginRequest req, HttpSession session) {
        FoodLoginResponse response = userService.login(req);
        if (response.isSuccess()) {
            userService.getUserById(response.getUserId()).ifPresent(user -> {
                session.setAttribute("foodUser", user);
                session.setAttribute("foodRole", user.getRole().name());
            });
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute("foodUser");
            session.removeAttribute("foodRole");
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @GetMapping({"/me", "/current"})
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        FoodUser user = bridgeService.resolveCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", true);
        data.put("userId", user.getId());
        data.put("name", user.getFullName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole().name());
        data.put("phoneNumber", user.getPhoneNumber());
        data.put("address", user.getAddress());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<FoodUser>> getAllCustomers() {
        return ResponseEntity.ok(userService.getAllCustomers());
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<FoodUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/delivery-partners")
    public ResponseEntity<List<FoodUser>> getAllDeliveryPartners() {
        return ResponseEntity.ok(userService.getAllDeliveryPartners());
    }

    @GetMapping("/users")
    public ResponseEntity<List<FoodUser>> getAllUsersAlias() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<FoodUser> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody FoodAddUserRequest req) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
