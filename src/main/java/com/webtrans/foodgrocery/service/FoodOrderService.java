package com.webtrans.foodgrocery.service;

import com.webtrans.foodgrocery.dto.FoodPlaceOrderRequest;
import com.webtrans.foodgrocery.entity.*;
import com.webtrans.foodgrocery.enums.OrderStatus;
import com.webtrans.foodgrocery.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodOrderService {

    private final FoodOrderRepository orderRepository;
    private final FoodUserRepository userRepository;
    private final FoodShopRepository shopRepository;
    private final FoodVendorRepository vendorRepository;
    private final FoodProductRepository productRepository;
    private final FoodStockRepository stockRepository;
    private final FoodOrderStatusHistoryRepository historyRepository;
    private final FoodDeliveryPersonRepository deliveryPersonRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public FoodOrder placeOrder(FoodPlaceOrderRequest req) {
        FoodUser customer = userRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + req.getCustomerId()));

        FoodShop shop = null;
        if (req.getShopId() != null) {
            shop = shopRepository.findById(req.getShopId()).orElse(null);
        }
        if (shop == null && req.getVendorId() != null) {
            List<FoodShop> vendorShops = shopRepository.findAll();
            if (!vendorShops.isEmpty()) {
                shop = vendorShops.get(0);
            }
        }
        if (shop == null) {
            List<FoodShop> anyShops = shopRepository.findByActiveTrue();
            if (!anyShops.isEmpty()) {
                shop = anyShops.get(0);
            } else {
                throw new RuntimeException("No active shop found to place order");
            }
        }

        FoodOrder order = new FoodOrder();
        order.setCustomer(customer);
        order.setShop(shop);
        order.setDeliveryAddress(req.getDeliveryAddress() != null && !req.getDeliveryAddress().isEmpty()
                ? req.getDeliveryAddress() : customer.getAddress());
        order.setSpecialInstructions(req.getSpecialInstructions());
        order.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "CASH_ON_DELIVERY");

        Double deliveryFee = req.getDeliveryFee() != null ? req.getDeliveryFee() :
                (shop.getDeliveryFee() != null ? shop.getDeliveryFee() : 150.0);
        order.setDeliveryFee(deliveryFee);
        order.setStatus(OrderStatus.PENDING);

        double total = 0.0;
        if (req.getItems() != null) {
            for (FoodPlaceOrderRequest.OrderItemRequest itemReq : req.getItems()) {
                FoodProduct product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

                // Update stock if tracked
                Optional<FoodStock> stockOpt = stockRepository.findByProductIdAndShopId(product.getId(), shop.getId());
                if (stockOpt.isEmpty()) {
                    List<FoodStock> prodStocks = stockRepository.findByProductId(product.getId());
                    if (!prodStocks.isEmpty()) {
                        stockOpt = Optional.of(prodStocks.get(0));
                    }
                }

                if (stockOpt.isPresent()) {
                    FoodStock stock = stockOpt.get();
                    if (stock.getQuantity() != null && stock.getQuantity() < itemReq.getQuantity()) {
                        throw new RuntimeException("Insufficient stock for product: " + product.getName());
                    }
                    if (stock.getQuantity() != null) {
                        stock.setQuantity(stock.getQuantity() - itemReq.getQuantity());
                        stockRepository.save(stock);
                        product.setStockQuantity(stock.getQuantity());
                        productRepository.save(product);
                    }
                }

                FoodOrderItem item = new FoodOrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(product.getPrice());
                order.getItems().add(item);
                total += product.getPrice() * itemReq.getQuantity();
            }
        }
        order.setTotalAmount(total + order.getDeliveryFee());

        FoodOrder saved = orderRepository.save(order);

        // Save history
        FoodOrderStatusHistory history = new FoodOrderStatusHistory();
        history.setOrder(saved);
        history.setStatus(OrderStatus.PENDING);
        history.setNotes("Order placed");
        historyRepository.save(history);

        // Notify via isolated Food live topic
        try {
            messagingTemplate.convertAndSend("/topic/food/orders", saved);
            messagingTemplate.convertAndSend("/topic/food/orders/" + saved.getId(), saved);
        } catch (Exception ignored) {}

        return saved;
    }

    public Optional<FoodOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Page<FoodOrder> getOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId, pageable);
    }

    public List<FoodOrder> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByPlacedAtAsc(status);
    }

    public List<FoodOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public FoodOrder payOrder(Long orderId, String paymentMethod) {
        FoodOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be paid in current status: " + order.getStatus());
        }

        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PREPARING);

        FoodOrder saved = orderRepository.save(order);

        FoodOrderStatusHistory history = new FoodOrderStatusHistory();
        history.setOrder(saved);
        history.setStatus(OrderStatus.PREPARING);
        history.setNotes("Payment completed via " + paymentMethod + ". Status updated to PREPARING.");
        historyRepository.save(history);

        try {
            messagingTemplate.convertAndSend("/topic/food/orders", saved);
            messagingTemplate.convertAndSend("/topic/food/orders/" + saved.getId(), saved);
        } catch (Exception ignored) {}

        return saved;
    }

    @Transactional
    public FoodOrder updateOrderStatus(Long orderId, OrderStatus newStatus, Long partnerId) {
        FoodOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(newStatus);

        if (partnerId != null && order.getDeliveryPartner() == null) {
            FoodDeliveryPerson partner = deliveryPersonRepository.findByUserId(partnerId)
                    .orElseGet(() -> deliveryPersonRepository.findById(partnerId)
                            .orElse(null));
            if (partner != null) {
                order.setDeliveryPartner(partner);
            }
        }

        FoodOrder saved = orderRepository.save(order);

        FoodOrderStatusHistory history = new FoodOrderStatusHistory();
        history.setOrder(saved);
        history.setStatus(newStatus);
        history.setNotes("Status updated to " + newStatus);
        historyRepository.save(history);

        try {
            messagingTemplate.convertAndSend("/topic/food/orders", saved);
            messagingTemplate.convertAndSend("/topic/food/orders/" + saved.getId(), saved);
        } catch (Exception ignored) {}

        return saved;
    }

    public List<FoodOrder> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    public List<FoodOrder> getOrdersByShop(Long shopId) {
        return orderRepository.findByShopId(shopId);
    }

    public Page<FoodOrder> searchOrders(Long customerId, Long shopId, String status, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Specification<FoodOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (customerId != null) predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            if (shopId != null) predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            if (status != null && !status.isEmpty()) predicates.add(cb.equal(root.get("status"), OrderStatus.valueOf(status.toUpperCase())));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("placedAt"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("placedAt"), toDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return orderRepository.findAll(spec, pageable);
    }
}
