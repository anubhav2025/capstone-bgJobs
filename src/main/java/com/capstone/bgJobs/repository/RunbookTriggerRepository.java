package com.capstone.bgJobs.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.bgJobs.model.runbook.Runbook;
import com.capstone.bgJobs.model.runbook.RunbookTrigger;

import java.util.List;

public interface RunbookTriggerRepository extends JpaRepository<RunbookTrigger, Long> {
    List<RunbookTrigger> findByRunbook(Runbook runbook);
}
