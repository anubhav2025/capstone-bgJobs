package com.capstone.bgJobs.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tenant_ticket")
public class TenantTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "ticket_id", nullable = false, unique = true)
    private String ticketId;

    @Column(name = "es_finding_id", nullable = false, unique = true)
    private String esFindingId;

    // Default constructor
    public TenantTicket() {
    }

    // Parameterized constructor
    public TenantTicket(String tenantId, String ticketId, String esFindingId) {
        this.tenantId = tenantId;
        this.ticketId = ticketId;
        this.esFindingId = esFindingId;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getEsFindingId() {
        return esFindingId;
    }

    public void setEsFindingId(String esFindingId) {
        this.esFindingId = esFindingId;
    }
}
