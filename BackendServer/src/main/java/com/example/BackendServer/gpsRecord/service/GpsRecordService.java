package com.example.BackendServer.gpsRecord.service;


import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.gpsRecord.db.GpsRecordRepository;
import com.example.BackendServer.gpsRecord.model.GpsRecordRequest;
import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GpsRecordService {

  private final GpsRecordRepository gpsRecordRepository;
  private final VehicleRepository vehicleRepository;
  private final RecordRepository recordRepository;

  @Transactional
  public GpsRecordEntity create(GpsRecordRequest gpsRecordRequest) {
    VehicleEntity vehicle = vehicleRepository.findById(gpsRecordRequest.getVehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle ID"));


    GpsRecordEntity.Status statusEnum;
    try {
      statusEnum = GpsRecordEntity.Status.valueOf(gpsRecordRequest.getStatus());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid status value");
    }

    // RecordEntity 연관 설정 (필요하면 추가, 아니면 null)
    RecordEntity record = null;

    GpsRecordEntity gpsRecord = GpsRecordEntity.builder()
        .vehicle(vehicle)
        .status(statusEnum)
        .latitude(gpsRecordRequest.getLatitude())
        .longitude(gpsRecordRequest.getLongitude())
        .oTime(gpsRecordRequest.getOTime())
        .gcd(gpsRecordRequest.getGcd())
        .totalDist(gpsRecordRequest.getTotalDist())
        .record(record) // nullable
        .build();

    return gpsRecordRepository.save(gpsRecord);
  }
}
