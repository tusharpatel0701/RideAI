package com.rideai.rideai_backend.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RideEventConsumer {

    @KafkaListener(topics = "ride-events")
    public void consume(String message) {
        System.out.println("Kafka event received: " + message);
    }
}