
package com.rideai.rideai_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.rideai.rideai_backend.entity.Driver;

import jakarta.persistence.LockModeType;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByAvailableTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.available = true ORDER BY d.id")
    List<Driver> findAvailableDriversForUpdate();
}
