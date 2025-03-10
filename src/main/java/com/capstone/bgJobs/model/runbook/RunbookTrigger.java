package com.capstone.bgJobs.model.runbook;


import com.capstone.bgJobs.enums.TriggerType;

import jakarta.persistence.*;

@Entity
@Table(name = "runbook_triggers")
public class RunbookTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runbook_id", nullable = false)
    private Runbook runbook;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    // Constructors
    public RunbookTrigger() {
    }

    public RunbookTrigger(Runbook runbook, TriggerType triggerType) {
        this.runbook = runbook;
        this.triggerType = triggerType;
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

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
}
