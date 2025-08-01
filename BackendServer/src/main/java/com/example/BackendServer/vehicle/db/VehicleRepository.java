package com.example.BackendServer.vehicle.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.BackendServer.global.Class.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    Page<VehicleEntity> findAllByStatusAndVehicleNumberContains(
      VehicleStatus status,
      String vehicleNumber,
      Pageable pageable
    );

    //상태 개수 가져오기
    long countByStatus(VehicleStatus status);
    // 삭제
    Optional<VehicleEntity> findByVehicleNumber(String vehicleNumber);
    // 차량 중복 확인
    boolean existsByVehicleNumber(String vehicleNumber);

}
