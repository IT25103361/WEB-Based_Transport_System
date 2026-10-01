package com.webtrans.Courier.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courier_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One parcel → one payment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id", nullable = false, unique = true)
    private Parcel parcel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "txn_ref", length = 50)
    private String txnRef;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.PENDING;
    }

    public enum Method {
        CARD, CASH, WALLET
    }

    public enum Status {
        PENDING, PAID, FAILED, REFUNDED
    }
}