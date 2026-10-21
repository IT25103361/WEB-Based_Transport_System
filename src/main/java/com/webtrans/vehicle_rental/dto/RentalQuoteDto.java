package com.webtrans.vehicle_rental.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class RentalQuoteDto {

    private Long vehicleId;
    private String vehicleName;
    private String vehicleType;
    private BigDecimal dailyRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private int billableDays;
    private BigDecimal subtotal;
    private BigDecimal refundableDeposit;
    private BigDecimal total;
    private String formattedDailyRate;
    private String formattedSubtotal;
    private String formattedDeposit;
    private String formattedTotal;
    private boolean available;
    private String availabilityMessage;
    private String dayCountExplanation;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    public RentalQuoteDto() {}

    public RentalQuoteDto(Long vehicleId, String vehicleName, String vehicleType,
                          BigDecimal dailyRate, LocalDate startDate, LocalDate endDate,
                          int billableDays, BigDecimal subtotal, BigDecimal total,
                          boolean available, String availabilityMessage, String dayCountExplanation) {
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.dailyRate = dailyRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billableDays = billableDays;
        this.subtotal = subtotal;
        this.refundableDeposit = BigDecimal.ZERO.setScale(2);
        this.total = total;
        this.formattedDailyRate = CURRENCY_FORMAT.format(dailyRate);
        this.formattedSubtotal = CURRENCY_FORMAT.format(subtotal);
        this.formattedDeposit = CURRENCY_FORMAT.format(BigDecimal.ZERO);
        this.formattedTotal = CURRENCY_FORMAT.format(total);
        this.available = available;
        this.availabilityMessage = availabilityMessage;
        this.dayCountExplanation = dayCountExplanation;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getBillableDays() { return billableDays; }
    public void setBillableDays(int billableDays) { this.billableDays = billableDays; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getRefundableDeposit() { return refundableDeposit; }
    public void setRefundableDeposit(BigDecimal refundableDeposit) { this.refundableDeposit = refundableDeposit; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getFormattedDailyRate() { return formattedDailyRate; }
    public void setFormattedDailyRate(String formattedDailyRate) { this.formattedDailyRate = formattedDailyRate; }

    public String getFormattedSubtotal() { return formattedSubtotal; }
    public void setFormattedSubtotal(String formattedSubtotal) { this.formattedSubtotal = formattedSubtotal; }

    public String getFormattedDeposit() { return formattedDeposit; }
    public void setFormattedDeposit(String formattedDeposit) { this.formattedDeposit = formattedDeposit; }

    public String getFormattedTotal() { return formattedTotal; }
    public void setFormattedTotal(String formattedTotal) { this.formattedTotal = formattedTotal; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getAvailabilityMessage() { return availabilityMessage; }
    public void setAvailabilityMessage(String availabilityMessage) { this.availabilityMessage = availabilityMessage; }

    public String getDayCountExplanation() { return dayCountExplanation; }
    public void setDayCountExplanation(String dayCountExplanation) { this.dayCountExplanation = dayCountExplanation; }
}
