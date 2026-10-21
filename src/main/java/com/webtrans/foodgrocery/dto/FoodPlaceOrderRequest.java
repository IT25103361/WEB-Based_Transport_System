package com.webtrans.foodgrocery.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPlaceOrderRequest {

    private Long customerId;

    private Long shopId;

    private Long vendorId;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    private String deliveryAddress;

    private String specialInstructions;

    private String paymentMethod;

    private Double deliveryFee;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
