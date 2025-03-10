package com.capstone.bgJobs.service;


import org.springframework.stereotype.Service;

import com.capstone.bgJobs.model.runbook.Runbook;
import com.capstone.bgJobs.model.runbook.RunbookAction;
import com.capstone.bgJobs.model.runbook.RunbookFilter;
import com.capstone.bgJobs.model.runbook.RunbookTrigger;
import com.capstone.bgJobs.repository.RunbookActionRepository;
import com.capstone.bgJobs.repository.RunbookFilterRepository;
import com.capstone.bgJobs.repository.RunbookRepository;
import com.capstone.bgJobs.repository.RunbookTriggerRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RunbookService {
    private final RunbookRepository runbookRepository;
    private final RunbookTriggerRepository triggerRepository;
    private final RunbookFilterRepository filterRepository;
    private final RunbookActionRepository actionRepository;

    public RunbookService(RunbookRepository runbookRepository,
                          RunbookTriggerRepository triggerRepository,
                          RunbookFilterRepository filterRepository,
                          RunbookActionRepository actionRepository) {
        this.runbookRepository = runbookRepository;
        this.triggerRepository = triggerRepository;
        this.filterRepository = filterRepository;
        this.actionRepository = actionRepository;
    }

    public List<Runbook> getRunbooksByTenant(String tenantId) {
        return runbookRepository.findByTenantId(tenantId);
    }

    public List<RunbookTrigger> getTriggersForRunbook(Runbook rb) {
        return triggerRepository.findByRunbook(rb);
    }

    public List<RunbookFilter> getFiltersForRunbook(Runbook rb) {
        return filterRepository.findByRunbook(rb);
    }

    public List<RunbookAction> getActionsForRunbook(Runbook rb) {
        return actionRepository.findByRunbook(rb);
    }
}
