package com.capstone.bgJobs.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.bgJobs.model.runbook.Runbook;
import com.capstone.bgJobs.model.runbook.RunbookAction;

import java.util.List;

public interface RunbookActionRepository extends JpaRepository<RunbookAction, Long> {
    List<RunbookAction> findByRunbook(Runbook runbook);
}

