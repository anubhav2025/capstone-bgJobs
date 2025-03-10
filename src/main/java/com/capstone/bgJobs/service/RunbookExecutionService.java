package com.capstone.bgJobs.service;

import com.capstone.bgJobs.dto.event.payload.StateUpdateJobEventPayload;
import com.capstone.bgJobs.enums.ToolTypes;
import com.capstone.bgJobs.model.Finding;
import com.capstone.bgJobs.model.FindingState;
import com.capstone.bgJobs.model.runbook.Runbook;
import com.capstone.bgJobs.model.runbook.RunbookAction;
import com.capstone.bgJobs.model.runbook.RunbookFilter;
import com.capstone.bgJobs.model.runbook.RunbookTrigger;
import com.capstone.bgJobs.service.AlertUpdateService;
import com.capstone.bgJobs.service.ElasticSearchService;
import com.capstone.bgJobs.service.JiraTicketService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunbookExecutionService {

    private final RunbookService runbookService;
    private final ElasticSearchService elasticSearchService;
    private final AlertUpdateService alertUpdateService;
    private final JiraTicketService jiraTicketService;

    public RunbookExecutionService(RunbookService runbookService,
                                   ElasticSearchService elasticSearchService,
                                   AlertUpdateService alertUpdateService,
                                   JiraTicketService jiraTicketService) {
        this.runbookService = runbookService;
        this.elasticSearchService = elasticSearchService;
        this.alertUpdateService = alertUpdateService;
        this.jiraTicketService = jiraTicketService;
    }

    public void executeRunbookJob(String tenantId, List<String> findingIds) {
        System.out.println("[executeRunbookJob] Starting runbooks for tenant=" + tenantId
            + ", #findingIds=" + findingIds.size());
    
        // 1) get runbooks
        List<Runbook> runbooks = runbookService.getRunbooksByTenant(tenantId);
        System.out.println("[executeRunbookJob] Found " + runbooks.size() 
            + " runbooks for tenant=" + tenantId);
    
        if (runbooks.isEmpty()) return;
    
        for (Runbook rb : runbooks) {
            System.out.println("[executeRunbookJob] Checking runbook ID=" + rb.getId() 
                + " (enabled=" + rb.isEnabled() + ", name=" + rb.getName() + ")");
    
            if (!rb.isEnabled()) {
                System.out.println(" -> This runbook is disabled, skipping.");
                continue;
            }
    
            // check triggers
            List<RunbookTrigger> triggers = runbookService.getTriggersForRunbook(rb);
            System.out.println(" -> runbook has " + triggers.size() + " triggers: " + triggers);
    
            boolean hasNewScanTrigger = triggers.stream()
                .anyMatch(t -> "NEW_SCAN_INITIATE".equalsIgnoreCase(t.getTriggerType().toString()));
                // if getTriggerType() is a String, no need for .toString()
    
            System.out.println(" -> hasNewScanTrigger=" + hasNewScanTrigger);
            if (!hasNewScanTrigger) {
                System.out.println(" -> No NEW_SCAN_INITIATE trigger, skipping runbook ID=" + rb.getId());
                continue;
            }
    
            // gather filter & actions
            List<RunbookFilter> filters = runbookService.getFiltersForRunbook(rb);
            List<RunbookAction> actions = runbookService.getActionsForRunbook(rb);
            System.out.println(" -> found filters=" + filters.size() + ", actions=" + actions.size());
    
            if (actions.isEmpty()) {
                System.out.println(" -> No actions, skipping runbook ID=" + rb.getId());
                continue;
            }
    
            RunbookFilter filter = filters.isEmpty() ? null : filters.get(0);
    
            // now loop over each finding
            for (String fId : findingIds) {
                List<Finding> results = elasticSearchService.searchFindingsById(tenantId, fId);
                if (results.isEmpty()) {
                    System.out.println(" -> Finding " + fId + " not found in ES, skipping.");
                    continue;
                }
    
                Finding f = results.get(0);
                System.out.println(" -> Processing finding " + fId + " with state=" + f.getState());
    
                if (!filterMatches(f, filter)) {
                    System.out.println(" -> filterMatches=false for finding " + fId + ", skipping actions.");
                    continue;
                }
    
                System.out.println(" -> filterMatches=true, applying runbook actions...");
    
                for (RunbookAction ra : actions) {
                    String actionType = ra.getActionType().toString();
                    System.out.println("    -> Checking action: " + actionType);
    
                    if ("UPDATE_FINDING".equalsIgnoreCase(actionType) && ra.getUpdateToState() != null) {
                        System.out.println("    -> doUpdateFinding => " + ra.getUpdateToState());
                        doUpdateFinding(tenantId, f, ra.getUpdateToState());
                    }
                    else if ("CREATE_TICKET".equalsIgnoreCase(actionType) && Boolean.TRUE.equals(ra.getCreateTicket())) {
                        System.out.println("    -> doCreateTicket");
                        doCreateTicket(tenantId, f);
                    }
                    else {
                        System.out.println("    -> Unknown or unselected action, skipping.");
                    }
                }
            }
        }
    }
    
    private boolean filterMatches(Finding f, RunbookFilter filter) {
        System.out.println("[filterMatches] For finding=" + f.getId() + " => " + f.getState());
        if (filter == null) {
            System.out.println(" -> No filter, matches by default.");
            return true;
        }
    
        if (filter.getState() != null && filter.getState() != f.getState()) {
            System.out.println(" -> State mismatch => filter=" + filter.getState() 
                + ", finding=" + f.getState());
            return false;
        }
    
        if (filter.getSeverity() != null && filter.getSeverity() != f.getSeverity()) {
            System.out.println(" -> Severity mismatch => filter=" + filter.getSeverity()
                + ", finding=" + f.getSeverity());
            return false;
        }
    
        System.out.println(" -> filterMatches=true");
        return true;
    }
    

    private void doUpdateFinding(String tenantId, Finding f, FindingState desiredState) {
        if (desiredState == null) return;
        // Use tool-specific reverse mapping by passing the tool type and desired state
        ReverseMappingResult mapping = reverseMapInternalToGh(f.getToolType(), desiredState);
        Object alertNumObj = f.getToolAdditionalProperties().get("number");
        String alertNumber = alertNumObj != null ? alertNumObj.toString() : "0";

        // Build payload for AlertUpdateService
        // (AlertUpdateService will then map these to GitHub-specific fields)
        StateUpdateJobEventPayload payload =
            new StateUpdateJobEventPayload();
        payload.setTenantId(tenantId);
        payload.setTool(f.getToolType());
        payload.setAlertNumber(alertNumber);
        payload.setEsFindingId(f.getId());
        payload.setUpdatedState(mapping.ghState);
        payload.setReason(mapping.ghReason);

        try {
            alertUpdateService.updateAlertState(payload);
            System.out.println("[RunbookExecutionService] Updated GH alert for finding=" 
                + f.getId() + " => state=" + mapping.ghState + ", reason=" + mapping.ghReason);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doCreateTicket(String tenantId, Finding f) {
        try {
            String summary = f.getTitle() == null ? "No Title" : f.getTitle();
            if (summary.length() > 200) summary = summary.substring(0, 200);
            String description = f.getDesc() == null ? "" : f.getDesc();
            if (description.length() > 200) description = description.substring(0, 200);
            jiraTicketService.createTicket(tenantId, f.getId(), summary, description);
            System.out.println("[RunbookExecutionService] Created ticket for finding=" + f.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ReverseMappingResult reverseMapInternalToGh(ToolTypes tool, FindingState internalState) {
        switch (tool) {
            case CODE_SCAN:
                if (internalState == FindingState.FALSE_POSITIVE) {
                    return new ReverseMappingResult("dismissed", "false positive");
                } else if (internalState == FindingState.FIXED) {
                    return new ReverseMappingResult("resolved", null);
                } else if (internalState == FindingState.SUPPRESSED) {
                    return new ReverseMappingResult("dismissed", "won't fix");
                } else {
                    // OPEN, CONFIRMED => "open"
                    return new ReverseMappingResult("open", null);
                }
    
            case DEPENDABOT:
                if (internalState == FindingState.FALSE_POSITIVE) {
                    // recognized reason => "inaccurate"
                    return new ReverseMappingResult("dismissed", "inaccurate");
                } else if (internalState == FindingState.FIXED) {
                    return new ReverseMappingResult("resolved", null);
                } else if (internalState == FindingState.SUPPRESSED) {
                    // must provide reason => e.g. "tolerable_risk"
                    return new ReverseMappingResult("dismissed", "tolerable_risk");
                } else {
                    // OPEN, CONFIRMED => "open"
                    return new ReverseMappingResult("open", null);
                }
    
            case SECRET_SCAN:
                if (internalState == FindingState.FALSE_POSITIVE) {
                    // "resolved" with "false_positive"
                    return new ReverseMappingResult("resolved", "false_positive");
                } else if (internalState == FindingState.FIXED) {
                    return new ReverseMappingResult("resolved", null);
                } else if (internalState == FindingState.SUPPRESSED) {
                    // "resolved" with "won't fix"
                    return new ReverseMappingResult("resolved", "won't fix");
                } else {
                    return new ReverseMappingResult("open", null);
                }
    
            default:
                return new ReverseMappingResult("open", null);
        }
    }
    
    

    private static class ReverseMappingResult {
        String ghState;
        String ghReason;
        ReverseMappingResult(String state, String reason) {
            this.ghState = state;
            this.ghReason = reason;
        }
    }
}
