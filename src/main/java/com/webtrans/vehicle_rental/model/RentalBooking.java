package com.webtrans.vehicle_rental.model;

import com.webtrans.auth.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "rent_bookings")
public class RentalBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private User customerUser;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    private RentalVehicle vehicle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_days")
    private Integer days;

    @Column(name = "daily_rate")
    private Double dailyRate;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "booking_status")
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED

    @Column(name = "extended_from_date")
    private LocalDate extendedFromDate;

    @Column(name = "extension_cost")
    private Double extensionCost;

    @Column(name = "extended_at")
    private LocalDateTime extendedAt;

    public RentalBooking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCustomerUser() { return customerUser; }
    public void setCustomerUser(User customerUser) { this.customerUser = customerUser; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public RentalVehicle getVehicle() { return vehicle; }
    public void setVehicle(RentalVehicle vehicle) { this.vehicle = vehicle; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }

    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getExtendedFromDate() { return extendedFromDate; }
    public void setExtendedFromDate(LocalDate extendedFromDate) { this.extendedFromDate = extendedFromDate; }

    public Double getExtensionCost() { return extensionCost; }
    public void setExtensionCost(Double extensionCost) { this.extensionCost = extensionCost; }

    public LocalDateTime getExtendedAt() { return extendedAt; }
    public void setExtendedAt(LocalDateTime extendedAt) { this.extendedAt = extendedAt; }

    /**
     * For interval calculation: same-day rentals span 1 day [start, start + 1).
     */
    public LocalDate getEffectiveEndDate() {
        if (startDate == null) return endDate;
        if (endDate == null) return startDate.plusDays(1);
        return startDate.equals(endDate) ? startDate.plusDays(1) : endDate;
    }

    public int calculateBillableDays() {
        if (startDate == null || endDate == null) return 1;
        long d = ChronoUnit.DAYS.between(startDate, endDate);
        return d < 1 ? 1 : (int) d;
    }
}
