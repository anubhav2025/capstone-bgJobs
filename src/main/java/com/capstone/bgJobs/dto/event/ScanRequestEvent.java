package com.capstone.bgJobs.dto.event;

import com.capstone.bgJobs.dto.event.payload.ScanRequestEventPayload;
import com.capstone.bgJobs.enums.EventTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public final class ScanRequestEvent implements Event<ScanRequestEventPayload> {

    public static final EventTypes TYPE = EventTypes.SCAN_REQUEST;

    private String eventId;
    private ScanRequestEventPayload payload;

    public ScanRequestEvent() {
    }

    public ScanRequestEvent(ScanRequestEventPayload payload) {
        this.payload = payload;
        this.eventId = UUID.randomUUID().toString(); // generate unique ID
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public EventTypes getType() {
        return TYPE;
    }

    @Override
    public ScanRequestEventPayload getPayload() {
        return payload;
    }
}
