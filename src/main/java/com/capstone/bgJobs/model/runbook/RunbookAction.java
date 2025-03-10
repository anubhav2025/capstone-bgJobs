package com.capstone.bgJobs.model.runbook;


import com.capstone.bgJobs.enums.ActionType;
import com.capstone.bgJobs.model.FindingState;

import jakarta.persistence.*;

@Entity
@Table(name = "runbook_actions")
public class RunbookAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runbook_id", nullable = false)
    private Runbook runbook;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    // For UPDATE_FINDING
    @Enumerated(EnumType.STRING)
    @Column(name = "update_to_state")
    private FindingState updateToState;

    // For CREATE_TICKET
    @Column(name = "create_ticket")
    private Boolean createTicket;

    // Constructors
    public RunbookAction() {
    }

    public RunbookAction(Runbook runbook, ActionType actionType, FindingState updateToState, Boolean createTicket) {
        this.runbook = runbook;
        this.actionType = actionType;
        this.updateToState = updateToState;
        this.createTicket = createTicket;
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

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public FindingState getUpdateToState() {
        return updateToState;
    }

    public void setUpdateToState(FindingState updateToState) {
        this.updateToState = updateToState;
    }

    public Boolean getCreateTicket() {
        return createTicket;
    }

    public void setCreateTicket(Boolean createTicket) {
        this.createTicket = createTicket;
    }
}
