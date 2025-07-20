package com.example.BackendServer.record.db;

import com.example.BackendServer.device.db.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordRepository extends JpaRepository<RecordEntity,Long> {

    Optional<RecordEntity> findTopByVehicleIdOrderByOnTimeDesc(Long vehicleId);

    Optional<RecordEntity> findByVehicleId(Long id);
}
