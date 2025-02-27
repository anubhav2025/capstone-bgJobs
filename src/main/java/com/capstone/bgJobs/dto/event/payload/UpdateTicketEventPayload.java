package com.capstone.bgJobs.dto.event.payload;

public class UpdateTicketEventPayload {
    private String tenantId;
    private String ticketId;

    public UpdateTicketEventPayload() {
    }

    public UpdateTicketEventPayload(String tenantId, String ticketId) {
        this.tenantId = tenantId;
        this.ticketId = ticketId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
    public String getTenantId() {
        return tenantId;
    }
    public String getTicketId() {
        return ticketId;
    }
}
