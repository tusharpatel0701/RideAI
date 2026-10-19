package com.rideai.rideai_backend.controller;


import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideai.rideai_backend.entity.Ride;
import com.rideai.rideai_backend.repository.RideRepository;
import com.rideai.rideai_backend.services.RideService;

@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "http://localhost:5173")
public class RideController {

    private final RideRepository rideRepository;

    private final RideService rideService;

    public RideController(RideRepository rideRepository, RideService rideService) {
        this.rideRepository = rideRepository;
        this.rideService = rideService;
    }
    
    @PostMapping
    public Ride createRide(@RequestBody Ride ride) {
        ride.setStatus("REQUESTED");
        return rideRepository.save(ride);
    }

    @GetMapping
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @PostMapping("/{id}/assign")
    public Ride assignDriver(
            @PathVariable Long id,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusKm) {

            return rideService.assignNearestDriver(id, latitude, longitude, radiusKm);
    }

    
    @PostMapping("/{id}/start")
    public Ride startRide(@PathVariable Long id) {
        return rideService.startRide(id);
    }

    @PostMapping("/{id}/complete")
    public Ride completeRide(@PathVariable Long id) {
        return rideService.completeRide(id);
    }

    

}
