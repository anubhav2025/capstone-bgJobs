package com.capstone.bgJobs.dto.event;

import com.capstone.bgJobs.dto.event.payload.ParseRequestEventPayload;
import com.capstone.bgJobs.enums.EventTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class ParseRequestEvent implements Event<ParseRequestEventPayload> {

    public static final EventTypes TYPE = EventTypes.PARSE_REQUEST;

    private String eventId;
    private ParseRequestEventPayload payload;

    public ParseRequestEvent() {
    }

    public ParseRequestEvent(ParseRequestEventPayload payload) {
        this.payload = payload;
        this.eventId = UUID.randomUUID().toString();
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
    public ParseRequestEventPayload getPayload() {
        return payload;
    }
}
