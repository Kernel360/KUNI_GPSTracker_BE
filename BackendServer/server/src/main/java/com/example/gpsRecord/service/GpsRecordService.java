package com.example.gpsRecord.service;

import com.example.global.Class.VehicleStatus;
import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
import com.example.entity.GpsRecordEntity;
import com.example.repository.GpsRecordRepository;
import com.example.gpsRecord.model.GpsRecordRequest;
import com.example.entity.RecordEntity;
import com.example.repository.RecordRepository;
import com.example.entity.VehicleEntity;
import com.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    VehicleStatus statusEnum;

    try {
      statusEnum = gpsRecordRequest.getStatus();

    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_VEHICLE_STATUS);
    }

    RecordEntity record = recordRepository.findByVehicleId(vehicle.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

    GpsRecordEntity gpsRecord = GpsRecordEntity.builder()
            .vehicle(vehicle)
            .status(statusEnum)
            .latitude(gpsRecordRequest.getLatitude())
            .longitude(gpsRecordRequest.getLongitude())
            .oTime(gpsRecordRequest.getOTime())
            .gcd(gpsRecordRequest.getGcd())
            .totalDist(gpsRecordRequest.getTotalDist())
            .record(record)
            .build();

    return gpsRecordRepository.save(gpsRecord);
  }

}
