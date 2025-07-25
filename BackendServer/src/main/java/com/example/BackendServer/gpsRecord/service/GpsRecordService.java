package com.example.BackendServer.gpsRecord.service;


import com.example.BackendServer.global.exception.CustomException;
import com.example.BackendServer.global.exception.ErrorCode;
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

import java.time.LocalDateTime;

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
        .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));


    GpsRecordEntity.Status statusEnum;
    try {
      statusEnum = GpsRecordEntity.Status.valueOf(gpsRecordRequest.getStatus());
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); //TODO : 임시, 나중에 적절한 ErrorCode 추가 권장
    }

    //TODO : RecordEntity 이후 수정 필요
    RecordEntity record = recordRepository.findByVehicleId(vehicle.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

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
