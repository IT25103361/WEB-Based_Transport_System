package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.entity.*;
import com.webtrans.foodgrocery.enums.DeliveryStatus;
import com.webtrans.foodgrocery.enums.OrderStatus;
import com.webtrans.foodgrocery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodDeliveryService {

    private final FoodDeliveryRepository deliveryRepository;
    private final FoodOrderRepository orderRepository;
    private final FoodDeliveryPersonRepository deliveryPersonRepository;
    private final FoodOrderStatusHistoryRepository historyRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<FoodDelivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public Optional<FoodDelivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    public Optional<FoodDelivery> getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }

    public List<FoodDelivery> getDeliveriesByPartner(Long partnerId) {
        return deliveryRepository.findByDeliveryPartnerId(partnerId);
    }

    public List<FoodDelivery> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status);
    }

    @Transactional
    public FoodDelivery createDelivery(Long orderId) {
        FoodOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Optional<FoodDelivery> existing = deliveryRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        FoodDelivery delivery = new FoodDelivery();
        delivery.setOrder(order);
        delivery.setDeliveryAddress(order.getDeliveryAddress());
        delivery.setStatus(DeliveryStatus.PENDING);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public FoodDelivery autoAssignDelivery() {
        FoodDelivery delivery = deliveryRepository.findAll().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.PENDING && d.getDeliveryPartner() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending deliveries found"));

        FoodDeliveryPerson partner = deliveryPersonRepository.findByIsAvailableTrue().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available delivery partners found"));

        partner.setAvailable(false);
        deliveryPersonRepository.save(partner);

        delivery.setDeliveryPartner(partner);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        FoodOrder order = delivery.getOrder();
        order.setDeliveryPartner(partner);
        orderRepository.save(order);

        FoodDelivery saved = deliveryRepository.save(delivery);
        try {
            messagingTemplate.convertAndSend("/topic/food/delivery/" + saved.getId(), saved);
        } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public FoodDelivery assignDelivery(Long deliveryId, Long partnerId) {
        FoodDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        FoodDeliveryPerson partner = deliveryPersonRepository.findByUserId(partnerId)
                .orElseGet(() -> deliveryPersonRepository.findById(partnerId)
                        .orElseThrow(() -> new RuntimeException("Delivery Person not found: " + partnerId)));

        partner.setAvailable(false);
        deliveryPersonRepository.save(partner);

        delivery.setDeliveryPartner(partner);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        FoodOrder order = delivery.getOrder();
        order.setDeliveryPartner(partner);
        orderRepository.save(order);

        FoodDelivery saved = deliveryRepository.save(delivery);
        try {
            messagingTemplate.convertAndSend("/topic/food/delivery/" + saved.getId(), saved);
        } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public FoodDelivery updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, String location,
                                         Double lat, Double lng) {
        FoodDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setStatus(newStatus);
        if (location != null) delivery.setCurrentLocation(location);
        if (lat != null) delivery.setLatitude(lat);
        if (lng != null) delivery.setLongitude(lng);

        FoodOrder order = delivery.getOrder();
        boolean statusChanged = false;

        if (newStatus == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
            order.setStatus(OrderStatus.ON_THE_WAY);
            statusChanged = true;
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            order.setStatus(OrderStatus.DELIVERED);
            statusChanged = true;

            if (delivery.getDeliveryPartner() != null) {
                FoodDeliveryPerson dp = delivery.getDeliveryPartner();
                dp.setAvailable(true);
                deliveryPersonRepository.save(dp);
            }
        } else if (newStatus == DeliveryStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            statusChanged = true;

            if (delivery.getDeliveryPartner() != null) {
                FoodDeliveryPerson dp = delivery.getDeliveryPartner();
                dp.setAvailable(true);
                deliveryPersonRepository.save(dp);
            }
        }

        if (statusChanged) {
            orderRepository.save(order);
            FoodOrderStatusHistory history = new FoodOrderStatusHistory();
            history.setOrder(order);
            history.setStatus(order.getStatus());
            history.setNotes("Delivery status changed to " + newStatus);
            historyRepository.save(history);
        }

        FoodDelivery saved = deliveryRepository.save(delivery);
        try {
            messagingTemplate.convertAndSend("/topic/food/delivery/" + saved.getId(), saved);
        } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public FoodDelivery updateLocation(Long deliveryId, String location, Double lat, Double lng) {
        FoodDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));
        if (location != null) delivery.setCurrentLocation(location);
        if (lat != null) delivery.setLatitude(lat);
        if (lng != null) delivery.setLongitude(lng);
        FoodDelivery saved = deliveryRepository.save(delivery);
        try {
            messagingTemplate.convertAndSend("/topic/food/delivery/" + saved.getId(), saved);
        } catch (Exception ignored) {}
        return saved;
    }

    public Map<String, Object> getTrackingDetails(Long orderId) {
        FoodOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Optional<FoodDelivery> deliveryOpt = deliveryRepository.findByOrderId(orderId);
        List<FoodOrderStatusHistory> history = historyRepository.findByOrderIdOrderByTimestampAsc(orderId);

        Map<String, Object> tracking = new HashMap<>();
        tracking.put("orderStatus", order.getStatus());
        tracking.put("placedAt", order.getPlacedAt());
        tracking.put("history", history);

        int deliveryTime = order.getShop() != null && order.getShop().getDeliveryTimeMinutes() != null
                ? order.getShop().getDeliveryTimeMinutes() : 30;
        tracking.put("estimatedTimeMinutes", deliveryTime);

        if (deliveryOpt.isPresent()) {
            FoodDelivery d = deliveryOpt.get();
            tracking.put("deliveryStatus", d.getStatus());
            tracking.put("currentLocation", d.getCurrentLocation());
            tracking.put("latitude", d.getLatitude());
            tracking.put("longitude", d.getLongitude());
            if (d.getDeliveryPartner() != null) {
                Map<String, String> partnerInfo = new HashMap<>();
                partnerInfo.put("name", d.getDeliveryPartner().getName());
                partnerInfo.put("phone", d.getDeliveryPartner().getPhone());
                partnerInfo.put("vehicleType", d.getDeliveryPartner().getVehicleType());
                tracking.put("deliveryPerson", partnerInfo);
            }
        }

        return tracking;
    }
}
