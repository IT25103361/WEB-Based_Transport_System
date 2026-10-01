package com.webtrans.Courier.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courier_parcels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_code", nullable = false, unique = true, length = 20)
    private String trackingCode;

    // Who is sending
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Receiver details (not a user account — just contact info)
    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_email", nullable = false, length = 120)
    private String receiverEmail;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    // Pickup
    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;

    @Column(name = "pickup_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLat;

    @Column(name = "pickup_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLng;

    // Drop-off
    @Column(name = "drop_address", nullable = false, length = 500)
    private String dropAddress;

    @Column(name = "drop_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal dropLat;

    @Column(name = "drop_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal dropLng;

    // Parcel info
    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_tier", nullable = false, length = 20)
    private ServiceTier serviceTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Column(name = "cancelled_by", length = 20)   // "SENDER" or "PARTNER"
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.REQUESTED;
        }
    }

    public enum Size {
        SMALL, MEDIUM, LARGE
    }

    public enum ServiceTier {
        INSTANT, ISLANDWIDE
    }

    public enum Status {
        REQUESTED, MATCHED, ACCEPTED, PICKED_UP, IN_TRANSIT,
        ARRIVED, DELIVERED, CANCELLED, FAILED, RETURNED
    }
}