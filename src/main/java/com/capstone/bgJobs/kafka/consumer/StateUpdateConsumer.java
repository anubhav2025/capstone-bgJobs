package com.capstone.bgJobs.kafka.consumer;

import com.capstone.bgJobs.dto.ack.AcknowledgementStatus;
import com.capstone.bgJobs.dto.ack.StateUpdateAcknowledgement;
import com.capstone.bgJobs.dto.ack.payload.AcknowledgementEventPayload;
import com.capstone.bgJobs.dto.event.StateUpdateJobEvent;
import com.capstone.bgJobs.dto.event.payload.StateUpdateJobEventPayload;
import com.capstone.bgJobs.enums.EventTypes;
import com.capstone.bgJobs.kafka.producer.StateUpdateAckProducerService;
import com.capstone.bgJobs.service.AlertUpdateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class StateUpdateConsumer {

    private final AlertUpdateService alertUpdateService;
    private final ObjectMapper objectMapper;
    private final StateUpdateAckProducerService ackProducer;

    public StateUpdateConsumer(AlertUpdateService alertUpdateService, StateUpdateAckProducerService ackProducer) {
        this.alertUpdateService = alertUpdateService;
        this.objectMapper = new ObjectMapper();
        this.ackProducer = ackProducer;
    }

    @KafkaListener(
        topics = "${spring.kafka.topics.bg_Jobs}",  // e.g. "bg-jobs"
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStateUpdate(@Payload String message) {
        try {
            System.out.println("[StateUpdateConsumer] raw message => " + message);

            // 1) parse to see if double-serialized
            JsonNode firstRoot = objectMapper.readTree(message);
            JsonNode realRoot = firstRoot;
            if (firstRoot.isTextual()) {
                String actualJson = firstRoot.asText();
                System.out.println("[StateUpdateConsumer] Double-serialized => " + actualJson);
                realRoot = objectMapper.readTree(actualJson);
            }

            // 2) check event type
            if (!realRoot.has("type")) {
                System.err.println("[StateUpdateConsumer] No 'type' => ignoring");
                return;
            }
            String typeStr = realRoot.get("type").asText();
            if (!EventTypes.UPDATE_FINDING.name().equals(typeStr)) {
                System.out.println("[StateUpdateConsumer] type=" + typeStr 
                    + " => ignoring (only handle UPDATE_FINDING).");
                return;
            }

            // 3) parse full event
            StateUpdateJobEvent event = objectMapper.treeToValue(realRoot, StateUpdateJobEvent.class);
            StateUpdateJobEventPayload payload = event.getPayload();

            System.out.println("[StateUpdateConsumer] Received UPDATE_FINDING => eventId=" 
                + event.getEventId() + ", tenant=" + payload.getTenantId());

            // 4) call the service to update the GH alert
            //    We'll adapt AlertUpdateService to accept StateUpdateJobEventPayload
            alertUpdateService.updateAlertState(payload);
            
            publishAck(event.getEventId(), true);


            System.out.println("[StateUpdateConsumer] State update completed for jobId=" + event.getEventId());
        } catch (Exception e) {
            System.err.println("[StateUpdateConsumer] Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void publishAck(String jobId, boolean success) {
        AcknowledgementEventPayload ackPayload = new AcknowledgementEventPayload();
        ackPayload.setJobId(jobId);
        ackPayload.setStatus(success ? AcknowledgementStatus.SUCCESS : AcknowledgementStatus.FAIL);

        StateUpdateAcknowledgement ack = new StateUpdateAcknowledgement(null, ackPayload);
        ackProducer.publishAcknowledgement(ack);
    }
}
