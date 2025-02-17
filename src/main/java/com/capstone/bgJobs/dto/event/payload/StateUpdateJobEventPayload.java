package com.capstone.bgJobs.dto.event.payload;

import com.capstone.bgJobs.enums.ToolTypes;
// import com.capstone.jfc.enums.FindingState;

public class StateUpdateJobEventPayload {
    private String esFindingId;
    private String tenantId;
    private ToolTypes tool;
    private String alertNumber;
    private String updatedState;
    private String reason;

    
    public StateUpdateJobEventPayload(String esFindingId, String tenantId, ToolTypes tool,
            String alertNumber, String updatedState, String reason) {
        this.esFindingId = esFindingId;
        this.tenantId = tenantId;
        this.tool = tool;
        this.alertNumber = alertNumber;
        this.updatedState = updatedState;
        this.reason = reason;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    } 
    public StateUpdateJobEventPayload() {}

    public String getEsFindingId() {
        return esFindingId;
    }
    public void setEsFindingId(String esFindingId) {
        this.esFindingId = esFindingId;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public ToolTypes getTool() {
        return tool;
    }
    public void setTool(ToolTypes tool) {
        this.tool = tool;
    }
    public String getAlertNumber() {
        return alertNumber;
    }
    public void setAlertNumber(String alertNumber) {
        this.alertNumber = alertNumber;
    }
    public String getUpdatedState() {
        return updatedState;
    }
    public void setUpdatedState(String updatedState) {
        this.updatedState = updatedState;
    }
}
