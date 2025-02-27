package com.capstone.bgJobs.dto.event;

import java.util.UUID;

import com.capstone.bgJobs.dto.event.payload.CreateTicketEventPayload;
import com.capstone.bgJobs.enums.EventTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTicketEvent implements Event<CreateTicketEventPayload> {
    public static final EventTypes TYPE = EventTypes.CREATE_TICKET;

    private String eventId;
    private CreateTicketEventPayload payload;

    public CreateTicketEvent() {
    }
    public CreateTicketEvent(CreateTicketEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }
    public String getEventId() {
        return eventId;
    }
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public EventTypes getType() {
        return TYPE;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public CreateTicketEventPayload getPayload() {
        return payload;
    }
    public void setPayload(CreateTicketEventPayload payload) {
        this.payload = payload;
    }
}
