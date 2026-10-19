
package com.rideai.rideai_backend.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.rideai.rideai_backend.dto.DriverLocationRequest;
import com.rideai.rideai_backend.entity.Driver;
import com.rideai.rideai_backend.repository.DriverRepository;
import com.rideai.rideai_backend.services.DriverLocationService;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:5173")
public class DriverController {

    private final DriverRepository driverRepository;
    private final DriverLocationService locationService;

    public DriverController(
            DriverRepository driverRepository,
            DriverLocationService locationService) {
        this.driverRepository = driverRepository;
        this.locationService = locationService;
    }

    @PostMapping
    public Driver createDriver(@RequestBody Driver driver) {
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Driver data is required");
        }

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

        validateCoordinates(latitude, longitude);

        if (!Double.isFinite(radiusKm)
                || radiusKm <= 0
                || radiusKm > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Radius must be greater than 0 and at most 100 km");
        }

        return driverRepository.findByAvailableTrue()
                .stream()
                .map(driver -> Map.entry(
                        driver,
                        calculateDistance(
                                latitude,
                                longitude,
                                driver.getLatitude(),
                                driver.getLongitude())))
                .filter(entry -> Double.isFinite(entry.getValue())
                        && entry.getValue() <= radiusKm)
                .sorted(Comparator.comparingDouble(Map.Entry<Driver, Double>::getValue))
                .map(Map.Entry::getKey)
                .toList();
    }

    private void validateCoordinates(double latitude, double longitude) {
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
    }

    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        if (!Double.isFinite(lat2)
                || lat2 < -90
                || lat2 > 90
                || !Double.isFinite(lon2)
                || lon2 < -180
                || lon2 > 180) {
            return Double.NaN;
        }

        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);

        double a = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * sinLon * sinLon;

        // Prevent floating-point rounding errors.
        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1.0 - a));

        return earthRadiusKm * c;
    }

    @PostMapping("/{id}/location")
    public Map<String, String> updateLocation(
            @PathVariable Long id,
            @RequestBody(required = false) DriverLocationRequest request) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid driver ID");
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location request body is required");
        }

        validateCoordinates(request.latitude(), request.longitude());

        if (!driverRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found");
        }

        locationService.updateLocation(
                id,
                request.latitude(),
                request.longitude());

        return Map.of("message", "Location updated in Redis");
    }

    @GetMapping("/{id}/location")
    public Map<String, String> getLocation(@PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid driver ID");
        }

        String location = locationService.getLocation(id);

        if (location == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Location not found or expired");
        }

        return Map.of(
                "driverId", id.toString(),
                "location", location);
    }
}
