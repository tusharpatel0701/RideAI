package com.rideai.rideai_backend.dto;

import java.time.Instant;

public record RideEvent(
        Long rideId,
        String eventType,
        Instant timestamp
) {}