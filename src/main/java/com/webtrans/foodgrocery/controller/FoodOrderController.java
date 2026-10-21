package com.webtrans.foodgrocery.controller;

import com.webtrans.foodgrocery.dto.FoodPlaceOrderRequest;
import com.webtrans.foodgrocery.entity.FoodOrder;
import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.enums.FoodRole;
import com.webtrans.foodgrocery.enums.OrderStatus;
import com.webtrans.foodgrocery.service.FoodCustomerBridgeService;
import com.webtrans.foodgrocery.service.FoodOrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food/orders")
@RequiredArgsConstructor
public class FoodOrderController {

    private final FoodOrderService orderService;
    private final FoodCustomerBridgeService bridgeService;

    @PostMapping
    public ResponseEntity<FoodOrder> placeOrder(@Valid @RequestBody FoodPlaceOrderRequest req, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        // Force server-authenticated customer ID to prevent spoofing
        req.setCustomerId(currentUser.getId());
        return ResponseEntity.ok(orderService.placeOrder(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodOrder> getOrderById(@PathVariable Long id, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return orderService.getOrderById(id)
                .map(order -> {
                    if (currentUser.getRole() == FoodRole.CUSTOMER && !order.getCustomer().getId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).<FoodOrder>build();
                    }
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/my-orders")
    public ResponseEntity<Page<FoodOrder>> getMyOrders(
            @RequestParam(required = false) Long customerId,
            Pageable pageable,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        Long resolvedCustomerId = (currentUser.getRole() == FoodRole.CUSTOMER || customerId == null) 
                ? currentUser.getId() 
                : customerId;
        return ResponseEntity.ok(orderService.getOrdersByCustomer(resolvedCustomerId, pageable));
    }

    @GetMapping("/admin/all-history")
    public ResponseEntity<Page<FoodOrder>> getAllHistory(Pageable pageable, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || currentUser.getRole() != FoodRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.searchOrders(null, null, null, null, null, pageable));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<FoodOrder>> searchOrderHistory(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        Long searchCustomer = customerId;
        if (currentUser.getRole() == FoodRole.CUSTOMER) {
            searchCustomer = currentUser.getId();
        }
        return ResponseEntity.ok(orderService.searchOrders(searchCustomer, shopId, status, fromDate, toDate, pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FoodOrder>> getPendingOrders(HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || (currentUser.getRole() != FoodRole.ADMIN && currentUser.getRole() != FoodRole.VENDOR_MANAGER)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.getPendingOrders());
    }

    @GetMapping("/all")
    public ResponseEntity<List<FoodOrder>> getAllOrders(HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || currentUser.getRole() == FoodRole.CUSTOMER) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<FoodOrder>> getOrdersByShop(@PathVariable Long shopId, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || currentUser.getRole() == FoodRole.CUSTOMER) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.getOrdersByShop(shopId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FoodOrder>> getOrdersByStatus(@PathVariable String status, HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(orderService.getOrdersByStatus(orderStatus));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FoodOrder> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null || currentUser.getRole() == FoodRole.CUSTOMER) {
            return ResponseEntity.status(403).build();
        }
        OrderStatus newStatus = OrderStatus.valueOf(body.get("status").toUpperCase());
        Long partnerId = body.containsKey("partnerId") && body.get("partnerId") != null
                ? Long.parseLong(body.get("partnerId")) : null;
        return ResponseEntity.ok(orderService.updateOrderStatus(id, newStatus, partnerId));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<FoodOrder> payOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        FoodUser currentUser = bridgeService.resolveCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        String paymentMethod = body.get("paymentMethod");
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            throw new RuntimeException("Payment method is required");
        }
        return ResponseEntity.ok(orderService.payOrder(id, paymentMethod));
    }
}
