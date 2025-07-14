package com.example.BackendServer.vehicle.db;

import com.example.BackendServer.vehicle.db.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    // 삭제
    Optional<VehicleEntity> findByVehicleNumber(String vehicleNumber);
}
