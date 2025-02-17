package com.capstone.bgJobs.dto;

public class AlertUpdateDTO {

    private String tenantId;   // Instead of owner+repo
    private String alertNumber;
    private String newState;
    private String reason;
    private String toolType;   // "CODE_SCAN","DEPENDABOT","SECRET_SCAN"

    // getters & setters
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAlertNumber() {
        return alertNumber;
    }
    public void setAlertNumber(String alertNumber) {
        this.alertNumber = alertNumber;
    }

    public String getNewState() {
        return newState;
    }
    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getToolType() {
        return toolType;
    }
    public void setToolType(String toolType) {
        this.toolType = toolType;
    }
}
