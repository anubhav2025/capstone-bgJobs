package com.capstone.bgJobs.dto.event.payload;

import com.capstone.bgJobs.enums.ToolTypes;

public final class ParseRequestEventPayload {

    private ToolTypes tool;
    private String tenantId;
    public ParseRequestEventPayload() {
    }

    private String filePath;

    public ParseRequestEventPayload(ToolTypes tool, String tenantId, String filePath) {
        this.tool = tool;
        this.tenantId = tenantId;
        this.filePath = filePath;
    }

    public ToolTypes getTool() {
        return tool;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getFilePath() {
        return filePath;
    }
}
 