package com.capstone.bgJobs.service;

// import com.capstone.bgJobs.dto.ticketing.TicketResponseDTO;
import com.capstone.bgJobs.model.Finding;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.model.TenantTicket;
import com.capstone.bgJobs.repository.TenantRepository;
import com.capstone.bgJobs.repository.TenantTicketRepository;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JiraTicketService {

    private final TenantRepository tenantRepository;
    private final TenantTicketRepository tenantTicketRepository;
    private final ElasticSearchService elasticSearchService;
    private final WebClient.Builder webClientBuilder;  // injected via constructor

    public JiraTicketService(TenantRepository tenantRepository,
                             TenantTicketRepository tenantTicketRepository,
                             ElasticSearchService elasticSearchService,
                             WebClient.Builder webClientBuilder) {
        this.tenantRepository = tenantRepository;
        this.tenantTicketRepository = tenantTicketRepository;
        this.elasticSearchService = elasticSearchService;
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Create a JIRA ticket and store references in ES and DB.
     */
    public String createTicket(String tenantId, String findingId, String summary, String description) {
        Tenant tenant = getTenantOrThrow(tenantId);

        // 1) Build request body for JIRA
        Map<String, Object> fieldsMap = new HashMap<>();
        Map<String, Object> projectMap = new HashMap<>();
        projectMap.put("key", tenant.getProjectKey()); // e.g. "CRM"

        Map<String, Object> issueTypeMap = new HashMap<>();
        issueTypeMap.put("name", "Bug"); // or "Task", "Story", etc.

        fieldsMap.put("project", projectMap);
        fieldsMap.put("summary", summary);
        fieldsMap.put("description", description);
        fieldsMap.put("issuetype", issueTypeMap);

        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fieldsMap);

        // 2) Make the POST request to JIRA
        String jiraCreateUrl = "https://"+ tenant.getAccountUrl() + "/rest/api/2/issue";
        
        // Using WebClient in a blocking manner by .block()
        Map<String, Object> responseBody = webClientBuilder.build()
            .post()
            .uri(jiraCreateUrl)
            .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        if (responseBody == null) {
            throw new RuntimeException("Failed to create JIRA issue. Empty response.");
        }

        // JIRA typically returns an object with keys: "id", "key", "self"
        String ticketKey = (String) responseBody.get("key"); // e.g. "CRM-11"

        // 3) Update the ES Finding with this ticketId
        Finding finding = retrieveSingleFinding(tenantId, findingId);
        System.out.println("hello");
        System.out.println(finding.getId());
        finding.setTicketId(ticketKey);
        elasticSearchService.saveFinding(tenantId, finding);

        // 4) Create TenantTicket mapping in DB
        TenantTicket tenantTicket = new TenantTicket(tenantId, ticketKey, findingId);
        tenantTicketRepository.save(tenantTicket);

        return ticketKey;
    }

    /**
     * Retrieve all JIRA issues for the given tenant by:
     *  1) finding all TenantTicket entries for that tenant
     *  2) for each ticketId, call JIRA to fetch the issue
     *  3) parse the response into a TicketResponseDTO
     */
    // public List<TicketResponseDTO> getAllTicketsForTenant(String tenantId) {
    //     Tenant tenant = getTenantOrThrow(tenantId);
    //     System.out.println(tenant.getTenantId());
    //     List<TenantTicket> tenantTickets = tenantTicketRepository.findAllByTenantId(tenantId);
    //     if (tenantTickets.isEmpty()) {
    //         return Collections.emptyList();
    //     }

    //     List<TicketResponseDTO> result = new ArrayList<>();
    //     for (TenantTicket tt : tenantTickets) {
    //         TicketResponseDTO dto = fetchTicketFromJira(tenant, tt.getTicketId());
    //         if (dto != null) {
    //             result.add(dto);
    //         }
    //     }
    //     return result;
    // }

    /**
     * Transition the ticket from "To Do" -> "Done".
     */
    public void updateTicketStatusToDone(String tenantId, String ticketId) {
        Tenant tenant = getTenantOrThrow(tenantId);
    
        while (true) {
            // 1) Fetch the available transitions for the current state
            String transitionsUrl = "https://" + tenant.getAccountUrl()
                    + "/rest/api/2/issue/"
                    + ticketId
                    + "/transitions?expand=transitions.fields";
    
            Map<String, Object> transitionsBody = webClientBuilder.build()
                .get()
                .uri(transitionsUrl)
                .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    
            if (transitionsBody == null) {
                throw new RuntimeException("Failed to fetch transitions for ticketId=" + ticketId);
            }
    
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transitions =
                    (List<Map<String, Object>>) transitionsBody.get("transitions");
    
            // 2) If there are no transitions, we stop. That means the issue is in its final state.
            if (transitions == null || transitions.isEmpty()) {
                // No more transitions => presumably the ticket is in "Done" or another final state.
                break;
            }
    
            // 3) Take the first transition in the list
            Map<String, Object> firstTransition = transitions.get(0);
            String transitionId = (String) firstTransition.get("id");
            if (transitionId == null) {
                throw new RuntimeException("Transition does not have an 'id'. " + firstTransition);
            }
    
            // 4) Apply this transition
            String transitionsPostUrl = "https://" + tenant.getAccountUrl() + "/rest/api/2/issue/" + ticketId + "/transitions";
    
            Map<String, Object> updatePayload = new HashMap<>();
            Map<String, Object> transitionObj = new HashMap<>();
            transitionObj.put("id", transitionId);
            updatePayload.put("transition", transitionObj);
    
            webClientBuilder.build()
                .post()
                .uri(transitionsPostUrl)
                .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updatePayload)
                .retrieve()
                .toBodilessEntity()
                .block();
    
            // Once the transition is applied, we loop again.
            // Now the issue is in a new state, so we fetch transitions again.
        }
    
        // By the time we exit the loop, no transitions remain.
        // That typically means the issue is in Done (or another final status).
    }
    

    // --------------------- Private Helpers ---------------------
    private Tenant getTenantOrThrow(String tenantId) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId);
        if (tenant == null) {
            throw new RuntimeException("Tenant not found for tenantId=" + tenantId);
        }
        return tenant;
    }

    /**
     * Generate Basic Auth header from email:apiToken
     */
    private String buildBasicAuthHeader(String email, String apiToken) {
        String auth = email + ":" + apiToken;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    /**
     * Fetch a single ticket from JIRA, parse the needed fields into a TicketResponseDTO
     */
    // private TicketResponseDTO fetchTicketFromJira(Tenant tenant, String ticketId) {
    //     String url = "https://" + tenant.getAccountUrl() + "/rest/api/2/issue/" + ticketId;

    //     Map<String, Object> body = webClientBuilder.build()
    //         .get()
    //         .uri(url)
    //         .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
    //         .retrieve()
    //         .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
    //         .block();

    //     if (body == null) {
    //         return null;
    //     }

    //     String key = (String) body.get("key"); // e.g. "CRM-11"
    //     Map<String, Object> fields = (Map<String, Object>) body.get("fields");
    //     if (fields == null) {
    //         return null;
    //     }

    //     // Issue type
    //     Map<String, Object> issueTypeMap = (Map<String, Object>) fields.get("issuetype");
    //     String issueTypeName = (String) issueTypeMap.get("name");
    //     String issueTypeDesc = (String) issueTypeMap.get("description");

    //     // Summary
    //     String summary = (String) fields.get("summary");

    //     // Status
    //     Map<String, Object> statusMap = (Map<String, Object>) fields.get("status");
    //     String statusName = statusMap != null ? (String) statusMap.get("name") : null;

    //     return new TicketResponseDTO(key, issueTypeName, issueTypeDesc, summary, statusName);
    // }

    /**
     * Retrieve a single Finding from ES to update its ticketId.
     */
    private Finding retrieveSingleFinding(String tenantId, String findingId) {
        List<Finding> results = elasticSearchService.searchFindingsById(tenantId, findingId);
        if (results.isEmpty()) {
            throw new RuntimeException("Finding not found in ES. ID=" + findingId);
        }
        return results.get(0);
    }
}
