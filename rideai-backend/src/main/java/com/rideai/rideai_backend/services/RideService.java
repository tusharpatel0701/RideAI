package com.rideai.rideai_backend.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.rideai.rideai_backend.entity.Driver;
import com.rideai.rideai_backend.entity.Ride;
import com.rideai.rideai_backend.repository.DriverRepository;
import com.rideai.rideai_backend.repository.RideRepository;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public RideService(RideRepository rideRepository, DriverRepository driverRepository) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Ride assignNearestDriver(
            Long rideId,
            double latitude,
            double longitude,
            double radiusKm) {

        if(!Double.isFinite(latitude)
                || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude)
                || longitude < -180 || longitude > 180
                || !Double.isFinite(radiusKm)
                || radiusKm <= 0 || radiusKm > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid coordinates or radius");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found"));

        if (!"REQUESTED".equals(ride.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ride is not awaiting assignment");
        }

        List<Driver> drivers = driverRepository.findAvailableDriversForUpdate();

        Driver nearest = drivers.stream().filter(d -> distanceKm(latitude, longitude,d.getLatitude(), d.getLongitude()) <= radiusKm)
                .min(Comparator.comparingDouble(d ->
                        distanceKm(
                                latitude, longitude,
                                d.getLatitude(), d.getLongitude()
                        )))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No available drivers nearby"));

        nearest.setAvailable(false);
        ride.setDriver(nearest);
        ride.setStatus("ASSIGNED");

        driverRepository.save(nearest);
        return rideRepository.save(ride);
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);

        return 6371 * 2 * Math.atan2(
                Math.sqrt(a), Math.sqrt(1 - a));
    }


    
        @Transactional
        public Ride startRide(Long rideId) {
                Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

                if (!"ASSIGNED".equals(ride.getStatus())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,"Only assigned rides can be started");
                }

                ride.setStatus("IN_PROGRESS");
                return rideRepository.save(ride);
        }

        @Transactional
        public Ride completeRide(Long rideId) {
                Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ride not found"));

                if (!"IN_PROGRESS".equals(ride.getStatus())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Only in-progress rides can be completed");
                }

                Driver driver = ride.getDriver();

                if (driver == null) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,"No driver assigned to this ride");
                }

                ride.setStatus("COMPLETED");
                driver.setAvailable(true);

                driverRepository.save(driver);
                return rideRepository.save(ride);
        }

}
