package com.example.BackendServer.location.service;

import com.example.BackendServer.global.exception.CustomException;
import com.example.BackendServer.global.exception.ErrorCode;
import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.gpsRecord.db.GpsRecordRepository;
import com.example.BackendServer.location.model.VehicleRealtimeInfoDto;
import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final GpsRecordRepository gpsRepository;

    /**
     * 위치 조회를 위한 정보 반환
     * 차량 정보와 실시간 위치를 반환한다.
     *
     * @param vehicleNumber : 차량 번호
     * @return VehicleRealtimeInfoDto
     */
    @Transactional(readOnly = true)
    public VehicleRealtimeInfoDto getVehicleRealtimeInfo(String vehicleNumber) {

        VehicleEntity vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        RecordEntity latestRecord = recordRepository
                .findTopByVehicleIdOrderByOnTimeDesc(vehicle.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        GpsRecordEntity latestGps = gpsRepository
                .findLatest(latestRecord.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GPS_RECORD_NOT_FOUND));

        //주행 시간
        LocalDateTime endTime = latestRecord.getOffTime();
        if(endTime == null) endTime = LocalDateTime.now();

        long drivingSeconds = Duration.between(
                latestRecord.getOnTime(),
                endTime
        ).getSeconds();

        //주행 거리
        double drivingDistanceKm = Double.parseDouble(latestRecord.getSumDist());

        return VehicleRealtimeInfoDto.builder()
                .vehicleNumber(vehicle.getVehicleNumber())
                .vehicleName(vehicle.getType())
                .drivingDate(latestRecord.getOnTime().toLocalDate())
                .drivingTime(drivingSeconds)
                .drivingDistanceKm(drivingDistanceKm)
                .location(
                        VehicleRealtimeInfoDto.Location.builder()
                                .onTime(latestGps.getOTime())
                                .latitude(latestGps.getLatitude())
                                .longitude(latestGps.getLongitude())
                                .build()
                )
                .build();
    }
}
