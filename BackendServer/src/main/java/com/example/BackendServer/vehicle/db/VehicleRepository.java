package com.example.BackendServer.vehicle.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
	Page<VehicleEntity> findAllByStatus(
		VehicleEntity.Status status,
		Pageable pageable
	);
}
