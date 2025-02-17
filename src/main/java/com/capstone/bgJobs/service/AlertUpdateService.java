package com.capstone.bgJobs.service;

import com.capstone.bgJobs.dto.event.payload.StateUpdateJobEventPayload;
import com.capstone.bgJobs.enums.ToolTypes;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlertUpdateService {

    private final TenantRepository tenantRepository;
    private final WebClient.Builder webClientBuilder;

    public AlertUpdateService(
            TenantRepository tenantRepository,
            WebClient.Builder webClientBuilder
    ) {
        this.tenantRepository = tenantRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public void updateAlertState(StateUpdateJobEventPayload payload) throws Exception {
        // 1) Find the tenant by tenantId
        Tenant tenant = tenantRepository.findByTenantId(payload.getTenantId());
        if (tenant == null) {
            throw new RuntimeException("No tenant found for tenantId: " + payload.getTenantId());
        }

        String token = tenant.getPat();

        // 2) Build GH endpoint from tenant + the tool
        String baseUrl = "https://api.github.com/repos/" 
            + tenant.getOwner() + "/" + tenant.getRepo();

        String patchUrl;
        ToolTypes toolTypeEnum = payload.getTool(); 
        switch (toolTypeEnum) {
            case CODE_SCAN:
                patchUrl = baseUrl + "/code-scanning/alerts/" + payload.getAlertNumber();
                break;
            case DEPENDABOT:
                patchUrl = baseUrl + "/dependabot/alerts/" + payload.getAlertNumber();
                break;
            case SECRET_SCAN:
                patchUrl = baseUrl + "/secret-scanning/alerts/" + payload.getAlertNumber();
                break;
            default:
                throw new IllegalStateException("Unknown tool type: " + toolTypeEnum);
        }

        // 3) Prepare request body
        Map<String, Object> patchRequest = new HashMap<>();
        String ghReason = mapReason(toolTypeEnum, payload.getReason());

        String newState = payload.getUpdatedState().toLowerCase();
        if (toolTypeEnum == ToolTypes.SECRET_SCAN) {
            if ("resolved".equalsIgnoreCase(newState)) {
                patchRequest.put("state", "resolved");
                if (ghReason != null) {
                    patchRequest.put("resolution", ghReason);
                }
            } else {
                patchRequest.put("state", "open");
            }
        } else {
            // CODE_SCAN or DEPENDABOT
            if ("dismissed".equalsIgnoreCase(newState)) {
                patchRequest.put("state", "dismissed");
                if (ghReason != null) {
                    patchRequest.put("dismissed_reason", ghReason);
                }
            } else {
                patchRequest.put("state", "open");
            }
        }

        WebClient webClient = webClientBuilder.build();
        System.out.println("PATCH URL: " + patchUrl);
        System.out.println("PATCH BODY: " + patchRequest);

        // 4) Perform the PATCH request
        webClient.patch()
                .uri(patchUrl)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .bodyValue(patchRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("[AlertUpdateService] GH alert updated => " + patchUrl);
        // Optionally re-trigger scanning or do anything else
    }

    /**
     * Map internal reason (e.g. "FALSE_POSITIVE") -> GH accepted strings
     */
    private String mapReason(ToolTypes toolType, String reason) {
        if (reason == null) return null;

        switch (toolType) {
            case CODE_SCAN:
                switch (reason.toUpperCase()) {
                    case "FALSE_POSITIVE": return "false positive";
                    case "WONT_FIX":       return "won't fix";
                    case "USED_IN_TESTS":  return "used in tests";
                    default: return null;
                }
            case DEPENDABOT:
                switch (reason.toUpperCase()) {
                    case "FIX_STARTED":    return "fix_started";
                    case "INACCURATE":     return "inaccurate";
                    case "NO_BANDWIDTH":   return "no_bandwidth";
                    case "NOT_USED":       return "not_used";
                    case "TOLERABLE_RISK": return "tolerable_risk";
                    default: return null;
                }
            case SECRET_SCAN:
                switch (reason.toUpperCase()) {
                    case "FALSE_POSITIVE": return "false_positive";
                    case "WONT_FIX":       return "won't fix";
                    case "REVOKED":        return "revoked";
                    case "USED_IN_TESTS":  return "used_in_tests";
                    default: return null;
                }
            default:
                return null;
        }
    }
}
