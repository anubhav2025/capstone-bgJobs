package com.capstone.bgJobs.dto.ack;

import java.util.UUID;

import com.capstone.bgJobs.dto.ack.payload.AcknowledgementEventPayload;

/**
 * This acknowledges completion (success/failure) of an update-finding job
 * in the bgJobs microservice, so JFC can update job status.
 */
public class StateUpdateAcknowledgement implements Acknowledgement<AcknowledgementEventPayload> {

    private String acknowledgementId;
    private AcknowledgementEventPayload payload;

    public StateUpdateAcknowledgement() {
        // for deserialization
    }

    public StateUpdateAcknowledgement(String acknowledgementId, AcknowledgementEventPayload payload) {
        // if user didn't specify an ID, generate one
        this.acknowledgementId = (acknowledgementId == null || acknowledgementId.isEmpty())
            ? UUID.randomUUID().toString()
            : acknowledgementId;
        this.payload = payload;
    }

    @Override
    public String getAcknowledgementId() {
        return acknowledgementId;
    }

    @Override
    public AcknowledgementEventPayload getPayload() {
        return payload;
    }

    public void setAcknowledgementId(String acknowledgementId) {
        this.acknowledgementId = acknowledgementId;
    }

    public void setPayload(AcknowledgementEventPayload payload) {
        this.payload = payload;
    }
}
