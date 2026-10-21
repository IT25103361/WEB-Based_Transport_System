package com.webtrans.admin.dto;

import java.time.LocalDateTime;

public class ActivityItemDto {
    private String id;
    private String moduleKey;
    private String moduleName;
    private String moduleBadgeColor;
    private String recordReference;
    private String description;
    private String status;
    private String statusBadgeClass;
    private LocalDateTime timestamp;
    private String formattedTimestamp;
    private String financialValue;

    public ActivityItemDto() {}

    public ActivityItemDto(String id, String moduleKey, String moduleName, String moduleBadgeColor,
                           String recordReference, String description, String status,
                           String statusBadgeClass, LocalDateTime timestamp,
                           String formattedTimestamp, String financialValue) {
        this.id = id;
        this.moduleKey = moduleKey;
        this.moduleName = moduleName;
        this.moduleBadgeColor = moduleBadgeColor;
        this.recordReference = recordReference;
        this.description = description;
        this.status = status;
        this.statusBadgeClass = statusBadgeClass;
        this.timestamp = timestamp;
        this.formattedTimestamp = formattedTimestamp;
        this.financialValue = financialValue;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleBadgeColor() { return moduleBadgeColor; }
    public void setModuleBadgeColor(String moduleBadgeColor) { this.moduleBadgeColor = moduleBadgeColor; }

    public String getRecordReference() { return recordReference; }
    public void setRecordReference(String recordReference) { this.recordReference = recordReference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusBadgeClass() { return statusBadgeClass; }
    public void setStatusBadgeClass(String statusBadgeClass) { this.statusBadgeClass = statusBadgeClass; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getFormattedTimestamp() { return formattedTimestamp; }
    public void setFormattedTimestamp(String formattedTimestamp) { this.formattedTimestamp = formattedTimestamp; }

    public String getFinancialValue() { return financialValue; }
    public void setFinancialValue(String financialValue) { this.financialValue = financialValue; }
}
