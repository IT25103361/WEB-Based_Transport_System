package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.entity.FoodDelivery;
import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.enums.DeliveryStatus;
import com.webtrans.foodgrocery.enums.FoodRole;
import com.webtrans.foodgrocery.service.FoodCustomerBridgeService;
import com.webtrans.foodgrocery.service.FoodDeliveryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodDeliveryController {

    private final FoodDeliveryService deliveryService;
    private final FoodCustomerBridgeService bridgeService;

    @GetMapping("/deliveries")
    public ResponseEntity<List<FoodDelivery>> getAllDeliveries(
            @RequestParam(required = false) String status,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || (currentUser.getRole() != FoodRole.ADMIN && currentUser.getRole() != FoodRole.VENDOR_MANAGER)) {
            return ResponseEntity.status(403).build();
        }
        if (status != null) {
            DeliveryStatus ds = DeliveryStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(ds));
        }
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/deliveries/{id}")
    public ResponseEntity<FoodDelivery> getDeliveryById(@PathVariable Long id, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return deliveryService.getDeliveryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/deliveries/order/{orderId}")
    public ResponseEntity<FoodDelivery> getDeliveryByOrder(@PathVariable Long orderId, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return deliveryService.getDeliveryByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/deliveries/partner/{partnerId}")
    public ResponseEntity<List<FoodDelivery>> getDeliveriesByPartner(@PathVariable Long partnerId, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || (currentUser.getRole() != FoodRole.ADMIN && currentUser.getRole() != FoodRole.DELIVERY_PARTNER)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(deliveryService.getDeliveriesByPartner(partnerId));
    }

    @PostMapping("/deliveries/order/{orderId}")
    public ResponseEntity<FoodDelivery> createDelivery(@PathVariable Long orderId, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || (currentUser.getRole() != FoodRole.ADMIN && currentUser.getRole() != FoodRole.VENDOR_MANAGER)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(deliveryService.createDelivery(orderId));
    }

    @PostMapping({"/delivery/assign", "/deliveries/assign"})
    public ResponseEntity<FoodDelivery> assignDelivery(HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || (currentUser.getRole() != FoodRole.ADMIN && currentUser.getRole() != FoodRole.VENDOR_MANAGER)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(deliveryService.autoAssignDelivery());
    }

    @PostMapping("/deliveries/{id}/assign")
    public ResponseEntity<FoodDelivery> manualAssignDelivery(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || (currentUser.getRole() != FoodRole.ADMIN && currentUser.getRole() != FoodRole.VENDOR_MANAGER)) {
            return ResponseEntity.status(403).build();
        }
        Long partnerId = Long.parseLong(body.get("partnerId").toString());
        return ResponseEntity.ok(deliveryService.assignDelivery(id, partnerId));
    }

    @PutMapping("/deliveries/{id}/status")
    public ResponseEntity<FoodDelivery> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || currentUser.getRole() == FoodRole.CUSTOMER) {
            return ResponseEntity.status(403).build();
        }
        DeliveryStatus newStatus = DeliveryStatus.valueOf(body.get("status").toString().toUpperCase());
        String location = body.containsKey("location") ? (String) body.get("location") : null;
        Double lat = body.containsKey("latitude") && body.get("latitude") != null
                ? Double.parseDouble(body.get("latitude").toString()) : null;
        Double lng = body.containsKey("longitude") && body.get("longitude") != null
                ? Double.parseDouble(body.get("longitude").toString()) : null;
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, newStatus, location, lat, lng));
    }

    @PutMapping("/deliveries/{id}/location")
    public ResponseEntity<FoodDelivery> updateLocation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || currentUser.getRole() == FoodRole.CUSTOMER) {
            return ResponseEntity.status(403).build();
        }
        String location = body.containsKey("location") ? (String) body.get("location") : null;
        Double lat = body.containsKey("latitude") && body.get("latitude") != null
                ? Double.parseDouble(body.get("latitude").toString()) : null;
        Double lng = body.containsKey("longitude") && body.get("longitude") != null
                ? Double.parseDouble(body.get("longitude").toString()) : null;
        return ResponseEntity.ok(deliveryService.updateLocation(id, location, lat, lng));
    }

    @GetMapping("/delivery/track/{orderId}")
    public ResponseEntity<Map<String, Object>> trackDelivery(@PathVariable Long orderId, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(deliveryService.getTrackingDetails(orderId));
    }

    @GetMapping("/customer/orders/{orderId}/track")
    public ResponseEntity<Map<String, Object>> customerTrackDelivery(@PathVariable Long orderId, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(deliveryService.getTrackingDetails(orderId));
    }
}
