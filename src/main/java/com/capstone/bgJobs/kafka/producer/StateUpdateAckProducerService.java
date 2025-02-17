package com.capstone.bgJobs.kafka.producer;

import com.capstone.bgJobs.dto.ack.Acknowledgement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes an acknowledgement message (StateUpdateAcknowledgement)
 * to the JFC's job-ack topic, so the JFC can mark the job success/failure.
 */
@Service
public class StateUpdateAckProducerService {

    @Value("${topics.job_ack:job-acknowledgement-topic}")
    private String ackTopic;  // Usually "job-acknowledgement-topic"

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public StateUpdateAckProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Publishes an Acknowledgement event (e.g. StateUpdateAcknowledgement)
     * to the job ack topic, letting JFC know the job status.
     */
    public void publishAcknowledgement(Acknowledgement<?> ack) {
        try {
            String ackJson = objectMapper.writeValueAsString(ack);
            kafkaTemplate.send(ackTopic, ackJson)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("[StateUpdateAckProducerService] Failed to send ack: " + ex.getMessage());
                    } else {
                        System.out.println("[StateUpdateAckProducerService] Ack sent => topic="
                            + ackTopic + ", partition=" + result.getRecordMetadata().partition()
                            + ", offset=" + result.getRecordMetadata().offset());
                    }
                });
        } catch (Exception e) {
            System.err.println("[StateUpdateAckProducerService] Error serializing ack: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
