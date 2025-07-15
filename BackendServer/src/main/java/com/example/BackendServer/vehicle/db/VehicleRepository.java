package com.example.BackendServer.vehicle.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    Page<VehicleEntity> findAllByStatusAndVehicleNumberContains(
      VehicleEntity.Status status,
      String vehicleNumber,
      Pageable pageable
    );

    // 삭제
    Optional<VehicleEntity> findByVehicleNumber(String vehicleNumber);
}
