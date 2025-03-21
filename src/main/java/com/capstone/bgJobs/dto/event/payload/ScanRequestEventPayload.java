package com.capstone.bgJobs.dto.event.payload;

import com.capstone.bgJobs.enums.ToolTypes;

public final class ScanRequestEventPayload {

    private ToolTypes tool;
    private String tenantId;

    public ScanRequestEventPayload() {
    }

    public ScanRequestEventPayload(ToolTypes tool, String tenantId) {
        this.tool = tool;
        this.tenantId = tenantId;
    }

    public ToolTypes getTool() {
        return tool;
    }

    public String getTenantId() {
        return tenantId;
    }
}
