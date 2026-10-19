
package com.rideai.rideai_backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import com.rideai.rideai_backend.dto.DriverLocationRequest;
import com.rideai.rideai_backend.repository.DriverRepository;
import com.rideai.rideai_backend.services.DriverLocationService;

@Controller
public class DriverLocationWebSocketController {

    private final DriverRepository driverRepository;
    private final DriverLocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    public DriverLocationWebSocketController(
            DriverRepository driverRepository,
            DriverLocationService locationService,
            SimpMessagingTemplate messagingTemplate) {

        this.driverRepository = driverRepository;
        this.locationService = locationService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/drivers/{id}/location")
    public void updateLocation(
            @DestinationVariable Long id,
            DriverLocationRequest request) {

        // Validate driver ID
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid driver ID");
        }

        // Validate request body
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location request is required");
        }

        double latitude = request.latitude();
        double longitude = request.longitude();

        // Validate latitude and longitude
        if (!Double.isFinite(latitude)
                || latitude < -90
                || latitude > 90
                || !Double.isFinite(longitude)
                || longitude < -180
                || longitude > 180) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid coordinates");
        }

        // Check whether the driver exists
        if (!driverRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Driver not found");
        }

        // Update the location in Redis
        locationService.updateLocation(
                id,
                latitude,
                longitude);

        // Prepare the WebSocket message
        Map<String, Object> payload = Map.of(
                "driverId", id,
                "latitude", latitude,
                "longitude", longitude
        );

        // Broadcast the updated location
        messagingTemplate.convertAndSend(
                "/topic/drivers/" + id + "/location",
                (Object) payload
        );
    }
}
