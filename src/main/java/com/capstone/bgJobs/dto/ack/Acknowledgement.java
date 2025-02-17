package com.capstone.bgJobs.dto.ack;

public interface Acknowledgement<T> {
    String getAcknowledgementId();
    T getPayload();
}
