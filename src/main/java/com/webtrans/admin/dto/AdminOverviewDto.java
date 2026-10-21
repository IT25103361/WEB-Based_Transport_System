package com.webtrans.admin.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminOverviewDto {
    // Filter & Meta
    private String filterPreset; // today, 7days, 30days, custom
    private String startDate;
    private String endDate;
    private String timezone = "Asia/Colombo (UTC+05:30)";
    private String lastRefreshed;

    // High Level KPIs
    private long totalSystemOperations;
    private long activeOperations;
    private long completedOperations;
    private long cancelledOperations;
    private long totalFleetCapacity; // drivers + buses + rental vehicles

    // Financial Metrics (Carefully labeled and segregated)
    private BigDecimal collectedRideRevenue = BigDecimal.ZERO; // From status='PAID' only
    private BigDecimal pendingRideFares = BigDecimal.ZERO;     // Active/pending rides estimated fare
    private BigDecimal agreedRentalValue = BigDecimal.ZERO;    // Confirmed/completed rental bookings
    private String formattedCollectedRideRevenue = "LKR 0.00";
    private String formattedPendingRideFares = "LKR 0.00";
    private String formattedAgreedRentalValue = "LKR 0.00";

    // Six Modules Summaries
    private List<ModuleSummaryDto> modules = new ArrayList<>();

    // Activity Trends Data for Chart
    private List<TrendPointDto> trends = new ArrayList<>();

    // Status Distribution Map for Donut Chart
    private Map<String, Long> statusDistribution = new LinkedHashMap<>();

    public AdminOverviewDto() {}

    public String getFilterPreset() { return filterPreset; }
    public void setFilterPreset(String filterPreset) { this.filterPreset = filterPreset; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getLastRefreshed() { return lastRefreshed; }
    public void setLastRefreshed(String lastRefreshed) { this.lastRefreshed = lastRefreshed; }

    public long getTotalSystemOperations() { return totalSystemOperations; }
    public void setTotalSystemOperations(long totalSystemOperations) { this.totalSystemOperations = totalSystemOperations; }

    public long getActiveOperations() { return activeOperations; }
    public void setActiveOperations(long activeOperations) { this.activeOperations = activeOperations; }

    public long getCompletedOperations() { return completedOperations; }
    public void setCompletedOperations(long completedOperations) { this.completedOperations = completedOperations; }

    public long getCancelledOperations() { return cancelledOperations; }
    public void setCancelledOperations(long cancelledOperations) { this.cancelledOperations = cancelledOperations; }

    public long getTotalFleetCapacity() { return totalFleetCapacity; }
    public void setTotalFleetCapacity(long totalFleetCapacity) { this.totalFleetCapacity = totalFleetCapacity; }

    public BigDecimal getCollectedRideRevenue() { return collectedRideRevenue; }
    public void setCollectedRideRevenue(BigDecimal collectedRideRevenue) { this.collectedRideRevenue = collectedRideRevenue; }

    public BigDecimal getPendingRideFares() { return pendingRideFares; }
    public void setPendingRideFares(BigDecimal pendingRideFares) { this.pendingRideFares = pendingRideFares; }

    public BigDecimal getAgreedRentalValue() { return agreedRentalValue; }
    public void setAgreedRentalValue(BigDecimal agreedRentalValue) { this.agreedRentalValue = agreedRentalValue; }

    public String getFormattedCollectedRideRevenue() { return formattedCollectedRideRevenue; }
    public void setFormattedCollectedRideRevenue(String formattedCollectedRideRevenue) { this.formattedCollectedRideRevenue = formattedCollectedRideRevenue; }

    public String getFormattedPendingRideFares() { return formattedPendingRideFares; }
    public void setFormattedPendingRideFares(String formattedPendingRideFares) { this.formattedPendingRideFares = formattedPendingRideFares; }

    public String getFormattedAgreedRentalValue() { return formattedAgreedRentalValue; }
    public void setFormattedAgreedRentalValue(String formattedAgreedRentalValue) { this.formattedAgreedRentalValue = formattedAgreedRentalValue; }

    public List<ModuleSummaryDto> getModules() { return modules; }
    public void setModules(List<ModuleSummaryDto> modules) { this.modules = modules; }

    public List<TrendPointDto> getTrends() { return trends; }
    public void setTrends(List<TrendPointDto> trends) { this.trends = trends; }

    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }
}
