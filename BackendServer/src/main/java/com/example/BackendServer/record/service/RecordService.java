package com.example.BackendServer.record.service;


import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

  private final RecordRepository recordRepository;
  private final VehicleRepository vehicleRepository;


  @Transactional
  public RecordEntity create(RecordRequest recordRequest) {
    VehicleEntity vehicle = vehicleRepository.findById(recordRequest.getVehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle ID"));



    RecordEntity record = RecordEntity.builder()
        .vehicle(vehicle)

        .sumDist(recordRequest.getSumDist())
        .onTime(recordRequest.getOnTime())
        .offTime(recordRequest.getOffTime())
        .build();

    return recordRepository.save(record);
  }
}
