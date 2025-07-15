package com.example.BackendServer.record.service;

import com.example.BackendServer.driver.db.DriverEntity;
import com.example.BackendServer.driver.db.DriverRepository;
import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordService {

  private final RecordRepository recordRepository;
  private final VehicleRepository vehicleRepository;
  private final DriverRepository driverRepository;

  public RecordEntity create(RecordRequest recordRequest) {
    VehicleEntity vehicle = vehicleRepository.findById(recordRequest.getVehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle ID"));
    DriverEntity driver = driverRepository.findById(recordRequest.getDriverId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid driver ID"));

    RecordEntity record = RecordEntity.builder()
        .vehicle(vehicle)
        .driver(driver)
        .sumDist(recordRequest.getSumDist())
        .onTime(recordRequest.getOnTime())
        .offTime(recordRequest.getOffTime())
        .build();

    return recordRepository.save(record);
  }
}
