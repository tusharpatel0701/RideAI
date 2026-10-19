
package com.rideai.rideai_backend.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.rideai.rideai_backend.entity.Driver;
import com.rideai.rideai_backend.repository.DriverRepository;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:5173")
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @PostMapping
    public Driver createDriver(@RequestBody Driver driver) {
        return driverRepository.save(driver);
    }

    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @GetMapping("/available")
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findByAvailableTrue();
    }

    @GetMapping("/nearby")
    public List<Driver> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusKm) {

        if (!Double.isFinite(latitude)
                || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude)
                || longitude < -180 || longitude > 180
                || !Double.isFinite(radiusKm)
                || radiusKm <= 0 || radiusKm > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid coordinates or radius"
            );
        }

        return driverRepository.findByAvailableTrue()
                .stream()
                .filter(driver -> {
                    double distance = calculateDistance(
                            latitude,
                            longitude,
                            driver.getLatitude(),
                            driver.getLongitude()
                    );

                    return distance <= radiusKm;
                })
                .sorted(Comparator.comparingDouble(driver ->
                        calculateDistance(
                                latitude,
                                longitude,
                                driver.getLatitude(),
                                driver.getLongitude()
                        )
                ))
                .toList();
    }

    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2)
                * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return earthRadiusKm * c;
    }
}
