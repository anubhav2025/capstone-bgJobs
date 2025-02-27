package com.capstone.bgJobs.dto.event;

import java.util.UUID;

import com.capstone.bgJobs.dto.event.payload.UpdateTicketEventPayload;
import com.capstone.bgJobs.enums.EventTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTicketEvent implements Event<UpdateTicketEventPayload>{
    public static final EventTypes TYPE = EventTypes.UPDATE_TICKET;

    private String eventId;
    private UpdateTicketEventPayload payload;

    public UpdateTicketEvent() {
    }
    public UpdateTicketEvent(UpdateTicketEventPayload payload) {
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
    public UpdateTicketEventPayload getPayload() {
        return payload;
    }
    public void setPayload(UpdateTicketEventPayload payload) {
        this.payload = payload;
    }
}
