package com.rideai.rideai_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rideai.rideai_backend.entity.Ride;

public interface RideRepository
        extends JpaRepository<Ride, Long> {
}