package com.webtrans.Courier.service;

import com.webtrans.Courier.dto.parcel.PayRequest;
import com.webtrans.Courier.dto.parcel.PaymentResponse;
import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.model.Payment;
import com.webtrans.Courier.model.User;
import com.webtrans.Courier.repository.ParcelRepository;
import com.webtrans.Courier.repository.PaymentRepository;
import com.webtrans.Courier.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ParcelRepository parcelRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public PaymentResponse pay(Long parcelId, PayRequest request, String senderEmail) {

        // 1. Find parcel, verify ownership
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + parcelId));

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (!parcel.getSender().getId().equals(sender.getId())) {
            throw new RuntimeException("You can only pay for your own parcels");
        }

        // 2. Prevent double payment
        if (paymentRepository.existsByParcelId(parcelId)) {
            Payment existing = paymentRepository.findByParcelId(parcelId).orElseThrow();
            if (existing.getStatus() == Payment.Status.PAID) {
                throw new RuntimeException("Parcel is already paid");
            }
        }

        // 3. Simulate a successful transaction
        // (In a real system: call gateway API, verify response)
        Payment payment = paymentRepository.findByParcelId(parcelId)
                .orElseGet(() -> Payment.builder().parcel(parcel).build());

        payment.setAmount(parcel.getPrice());
        payment.setMethod(request.getMethod());
        payment.setStatus(Payment.Status.PAID);
        payment.setTxnRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        return toResponse(saved);
    }

    public PaymentResponse getPayment(Long parcelId, String senderEmail) {

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + parcelId));

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (!parcel.getSender().getId().equals(sender.getId())) {
            throw new RuntimeException("You can only view payments for your own parcels");
        }

        Payment payment = paymentRepository.findByParcelId(parcelId)
                .orElseThrow(() -> new RuntimeException("No payment found for this parcel"));

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .parcelId(p.getParcel().getId())
                .trackingCode(p.getParcel().getTrackingCode())
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .txnRef(p.getTxnRef())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .build();
    }
}