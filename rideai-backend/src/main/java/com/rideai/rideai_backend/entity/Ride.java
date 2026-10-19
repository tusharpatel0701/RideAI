package com.rideai.rideai_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String passengerName;
    private String pickupAddress;
    private String destinationAddress;

    private BigDecimal estimatedFare;

    private String status = "REQUESTED";

    private LocalDateTime createdAt = LocalDateTime.now();


    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    public Ride() {}

    public Long getId() { 
        return id; 
    }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String name) {
        this.passengerName = name;
    }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String address) {
        this.pickupAddress = address;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }
    public void setDestinationAddress(String address) {
        this.destinationAddress = address;
    }

    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal fare) {
        this.estimatedFare = fare;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

}