package com.webtrans.admin.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleSummaryDto {
    private String moduleKey;
    private int moduleNumber;
    private String moduleName;
    private String statusBadge; // "ACTIVE" or "IN_PROGRESS"
    private String iconClass;
    private String accentColor;
    private long totalRecords;
    private long activeRecords;
    private long completedRecords;
    private long cancelledRecords;
    private long resourceCount;
    private String resourceLabel;
    private String headlineMetricValue;
    private String headlineMetricLabel;
    private Map<String, String> subMetrics = new LinkedHashMap<>();
    private Map<String, Long> statusDistribution = new LinkedHashMap<>();
    private boolean hasRealData;
    private String statusMessage;

    public ModuleSummaryDto() {}

    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }

    public int getModuleNumber() { return moduleNumber; }
    public void setModuleNumber(int moduleNumber) { this.moduleNumber = moduleNumber; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getStatusBadge() { return statusBadge; }
    public void setStatusBadge(String statusBadge) { this.statusBadge = statusBadge; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }

    public long getActiveRecords() { return activeRecords; }
    public void setActiveRecords(long activeRecords) { this.activeRecords = activeRecords; }

    public long getCompletedRecords() { return completedRecords; }
    public void setCompletedRecords(long completedRecords) { this.completedRecords = completedRecords; }

    public long getCancelledRecords() { return cancelledRecords; }
    public void setCancelledRecords(long cancelledRecords) { this.cancelledRecords = cancelledRecords; }

    public long getResourceCount() { return resourceCount; }
    public void setResourceCount(long resourceCount) { this.resourceCount = resourceCount; }

    public String getResourceLabel() { return resourceLabel; }
    public void setResourceLabel(String resourceLabel) { this.resourceLabel = resourceLabel; }

    public String getHeadlineMetricValue() { return headlineMetricValue; }
    public void setHeadlineMetricValue(String headlineMetricValue) { this.headlineMetricValue = headlineMetricValue; }

    public String getHeadlineMetricLabel() { return headlineMetricLabel; }
    public void setHeadlineMetricLabel(String headlineMetricLabel) { this.headlineMetricLabel = headlineMetricLabel; }

    public Map<String, String> getSubMetrics() { return subMetrics; }
    public void setSubMetrics(Map<String, String> subMetrics) { this.subMetrics = subMetrics; }

    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }

    public boolean isHasRealData() { return hasRealData; }
    public void setHasRealData(boolean hasRealData) { this.hasRealData = hasRealData; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
