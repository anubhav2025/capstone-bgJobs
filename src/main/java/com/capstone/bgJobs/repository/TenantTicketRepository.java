package com.capstone.bgJobs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.capstone.bgJobs.model.TenantTicket;
import java.util.List;
import java.util.Optional;

public interface TenantTicketRepository extends JpaRepository<TenantTicket, Long> {

    Optional<TenantTicket> findByTicketId(String ticketId);

    Optional<TenantTicket> findByEsFindingId(String esFindingId);

    // Retrieve all tickets for a given tenantId
    List<TenantTicket> findAllByTenantId(String tenantId);
}
