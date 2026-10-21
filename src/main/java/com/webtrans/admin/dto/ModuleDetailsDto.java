package com.webtrans.admin.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleDetailsDto {
    private String moduleKey;
    private String moduleName;
    private String description;
    private String statusBadge;
    private String accentColor;
    private String iconClass;
    private boolean inProgress;
    private String integrationNotes;

    // Granular tables/items depending on module
    private List<Map<String, Object>> resources = new ArrayList<>();
    private List<Map<String, Object>> records = new ArrayList<>();
    private Map<String, Object> extraStats;

    public ModuleDetailsDto() {}

    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatusBadge() { return statusBadge; }
    public void setStatusBadge(String statusBadge) { this.statusBadge = statusBadge; }

    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public boolean isInProgress() { return inProgress; }
    public void setInProgress(boolean inProgress) { this.inProgress = inProgress; }

    public String getIntegrationNotes() { return integrationNotes; }
    public void setIntegrationNotes(String integrationNotes) { this.integrationNotes = integrationNotes; }

    public List<Map<String, Object>> getResources() { return resources; }
    public void setResources(List<Map<String, Object>> resources) { this.resources = resources; }

    public List<Map<String, Object>> getRecords() { return records; }
    public void setRecords(List<Map<String, Object>> records) { this.records = records; }

    public Map<String, Object> getExtraStats() { return extraStats; }
    public void setExtraStats(Map<String, Object> extraStats) { this.extraStats = extraStats; }
}
