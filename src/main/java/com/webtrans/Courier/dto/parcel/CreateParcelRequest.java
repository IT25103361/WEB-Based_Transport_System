package com.webtrans.Courier.dto.parcel;

import com.webtrans.Courier.model.Parcel;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateParcelRequest {

    // ---------- Receiver info ----------
    @NotBlank(message = "Receiver name is required")
    @Size(max = 100)
    private String receiverName;

    @NotBlank(message = "Receiver email is required")
    @Email(message = "Invalid receiver email")
    private String receiverEmail;

    @Size(max = 20)
    private String receiverPhone;


    // ---------- Pickup ----------
    @Size(max = 500)
    private String pickupAddress;          // optional — map pin is the source of truth

    @NotNull(message = "Pickup latitude is required")
    private BigDecimal pickupLat;

    @NotNull(message = "Pickup longitude is required")
    private BigDecimal pickupLng;

    // ---------- Drop ----------
    @Size(max = 500)
    private String dropAddress;            // optional

    @NotNull(message = "Drop latitude is required")
    private BigDecimal dropLat;

    @NotNull(message = "Drop longitude is required")
    private BigDecimal dropLng;

    // ---------- Parcel info ----------
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    private BigDecimal weightKg;

    @NotNull(message = "Size is required")
    private Parcel.Size size;

    @NotNull(message = "Service tier is required")
    private Parcel.ServiceTier serviceTier;

    // For ISLANDWIDE only
    private LocalDateTime scheduledAt;
}