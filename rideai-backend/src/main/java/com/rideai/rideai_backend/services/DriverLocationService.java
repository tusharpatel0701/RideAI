package com.rideai.rideai_backend.services;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DriverLocationService {

    private final StringRedisTemplate redisTemplate;

    public DriverLocationService(
            StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateLocation(
            Long driverId,
            double latitude,
            double longitude) {

        String key = "driver:location:" + driverId;
        String value = latitude + "," + longitude;

        redisTemplate.opsForValue().set(
                key, value, Duration.ofMinutes(2));
    }

    public String getLocation(Long driverId) {
        return redisTemplate.opsForValue().get("driver:location:" + driverId);
    }
}

