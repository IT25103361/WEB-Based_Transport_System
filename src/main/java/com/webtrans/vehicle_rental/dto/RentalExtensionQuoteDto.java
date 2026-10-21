package com.webtrans.vehicle_rental.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class RentalExtensionQuoteDto {

    private Long bookingId;
    private String vehicleName;
    private LocalDate currentEndDate;
    private LocalDate newEndDate;
    private int additionalDays;
    private BigDecimal dailyRate;
    private BigDecimal additionalCost;
    private BigDecimal previousTotalCost;
    private BigDecimal newTotalCost;
    private String formattedAdditionalCost;
    private String formattedNewTotalCost;
    private boolean available;
    private String message;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    public RentalExtensionQuoteDto() {}

    public RentalExtensionQuoteDto(Long bookingId, String vehicleName,
                                   LocalDate currentEndDate, LocalDate newEndDate,
                                   int additionalDays, BigDecimal dailyRate,
                                   BigDecimal additionalCost, BigDecimal previousTotalCost,
                                   BigDecimal newTotalCost, boolean available, String message) {
        this.bookingId = bookingId;
        this.vehicleName = vehicleName;
        this.currentEndDate = currentEndDate;
        this.newEndDate = newEndDate;
        this.additionalDays = additionalDays;
        this.dailyRate = dailyRate;
        this.additionalCost = additionalCost;
        this.previousTotalCost = previousTotalCost;
        this.newTotalCost = newTotalCost;
        this.formattedAdditionalCost = CURRENCY_FORMAT.format(additionalCost);
        this.formattedNewTotalCost = CURRENCY_FORMAT.format(newTotalCost);
        this.available = available;
        this.message = message;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public LocalDate getCurrentEndDate() { return currentEndDate; }
    public void setCurrentEndDate(LocalDate currentEndDate) { this.currentEndDate = currentEndDate; }

    public LocalDate getNewEndDate() { return newEndDate; }
    public void setNewEndDate(LocalDate newEndDate) { this.newEndDate = newEndDate; }

    public int getAdditionalDays() { return additionalDays; }
    public void setAdditionalDays(int additionalDays) { this.additionalDays = additionalDays; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public BigDecimal getAdditionalCost() { return additionalCost; }
    public void setAdditionalCost(BigDecimal additionalCost) { this.additionalCost = additionalCost; }

    public BigDecimal getPreviousTotalCost() { return previousTotalCost; }
    public void setPreviousTotalCost(BigDecimal previousTotalCost) { this.previousTotalCost = previousTotalCost; }

    public BigDecimal getNewTotalCost() { return newTotalCost; }
    public void setNewTotalCost(BigDecimal newTotalCost) { this.newTotalCost = newTotalCost; }

    public String getFormattedAdditionalCost() { return formattedAdditionalCost; }
    public void setFormattedAdditionalCost(String formattedAdditionalCost) { this.formattedAdditionalCost = formattedAdditionalCost; }

    public String getFormattedNewTotalCost() { return formattedNewTotalCost; }
    public void setFormattedNewTotalCost(String formattedNewTotalCost) { this.formattedNewTotalCost = formattedNewTotalCost; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
