package com.capstone.bgJobs.model.runbook;



import com.capstone.bgJobs.model.FindingSeverity;
import com.capstone.bgJobs.model.FindingState;

import jakarta.persistence.*;

@Entity
@Table(name = "runbook_filters")
public class RunbookFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runbook_id", nullable = false)
    private Runbook runbook;

    @Enumerated(EnumType.STRING)
    private FindingState state;      // can be null to mean "any state"

    @Enumerated(EnumType.STRING)
    private FindingSeverity severity; // can be null to mean "any severity"

    // Constructors
    public RunbookFilter() {
    }

    public RunbookFilter(Runbook runbook, FindingState state, FindingSeverity severity) {
        this.runbook = runbook;
        this.state = state;
        this.severity = severity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Runbook getRunbook() {
        return runbook;
    }

    public void setRunbook(Runbook runbook) {
        this.runbook = runbook;
    }

    public FindingState getState() {
        return state;
    }

    public void setState(FindingState state) {
        this.state = state;
    }

    public FindingSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(FindingSeverity severity) {
        this.severity = severity;
    }
}
