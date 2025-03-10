package com.capstone.bgJobs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.bgJobs.model.runbook.Runbook;
import com.capstone.bgJobs.model.runbook.RunbookFilter;

import java.util.List;

public interface RunbookFilterRepository extends JpaRepository<RunbookFilter, Long> {
    List<RunbookFilter> findByRunbook(Runbook runbook);
}

