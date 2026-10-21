package com.rideai.rideai_backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideai.rideai_backend.dto.RideEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RideEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public RideEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(Long rideId, String eventType) {
        try {
            RideEvent event = new RideEvent(
                    rideId, eventType, Instant.now());

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    "ride-events",
                    rideId.toString(),
                    json
            ).whenComplete((result, error) -> {
                if (error != null) {
                    System.err.println(
                            "Kafka publish failed: " + error.getMessage());
                } else {
                    System.out.println(
                            "Kafka event published: " + json);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create ride event", e);
        }
    }
}