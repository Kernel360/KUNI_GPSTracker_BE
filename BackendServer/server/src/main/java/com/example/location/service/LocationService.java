package com.example.location.service;

import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
import com.example.entity.GpsRecordEntity;
import com.example.repository.GpsRecordRepository;
import com.example.location.model.Location;
import com.example.location.model.VehicleRealtimeInfoDto;
import com.example.entity.RecordEntity;
import com.example.repository.RecordRepository;
import com.example.entity.VehicleEntity;
import com.example.repository.VehicleRepository;
import com.example.global.Class.VehicleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final GpsRecordRepository gpsRepository;

    public VehicleRealtimeInfoDto getVehicleRealtimeInfo(String vehicleNumber) {

        // 1️⃣ 차량 정보 조회
        VehicleEntity vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
            .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        // 2️⃣ 최신 운행 기록 조회
        RecordEntity latestRecord = recordRepository
            .findTopByVehicleIdOrderByOnTimeDesc(vehicle.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 3️⃣ 해당 운행의 GPS 데이터 조회
        List<GpsRecordEntity> gpsList = gpsRepository.findByRecordIdOrderByOTime(latestRecord.getId());
        if (gpsList.isEmpty()) {
            throw new CustomException(ErrorCode.GPS_RECORD_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoMinutesAgo = now.minusMinutes(2);

        // 4️⃣ 2분 전 기준 GPS 선택
        GpsRecordEntity twoMinGps = gpsList.stream()
            .filter(gps -> !gps.getOTime().isAfter(twoMinutesAgo))
            .max(Comparator.comparing(GpsRecordEntity::getOTime))
            .orElse(null);

        // 5️⃣ 상태 결정
        VehicleStatus status;
        if (twoMinGps != null) {
            status = vehicle.getStatus(); // 2분 전 데이터가 있으면 기존 상태 유지
        } else {
            // 2분 전 데이터가 없으면 2분 전 ~ 현재 사이 데이터 확인
            boolean existsBetween = gpsList.stream()
                .anyMatch(gps -> !gps.getOTime().isBefore(twoMinutesAgo) && !gps.getOTime().isAfter(now));
            status = existsBetween ? vehicle.getStatus() : VehicleStatus.INACTIVE;
        }

        // 6️⃣ 사용할 GPS 선택 (2분 전 데이터 없으면 가장 최근 사용)
        GpsRecordEntity targetGps = (twoMinGps != null) ? twoMinGps : gpsList.get(gpsList.size() - 1);

        // 7️⃣ 주행 시간 계산
        LocalDateTime endTime = latestRecord.getOffTime() != null ? latestRecord.getOffTime() : now;
        long drivingMinutes = Duration.between(latestRecord.getOnTime(), endTime).toMinutes();
        if (drivingMinutes < 0) drivingMinutes = 0;

        // 8️⃣ 주행 거리 계산
        double drivingDistanceKm;
        try {
            drivingDistanceKm = Double.parseDouble(targetGps.getTotalDist());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_RECORD_DURATION);
        }

        // 9️⃣ 운행 날짜
        LocalDate drivingDate = latestRecord.getOnTime() != null ? latestRecord.getOnTime().toLocalDate() : null;

        // 🔟 DTO 반환
        return VehicleRealtimeInfoDto.builder()
            .vehicleNumber(vehicle.getVehicleNumber())
            .vehicleName(vehicle.getType())
            .drivingDate(drivingDate)
            .drivingTime(drivingMinutes)
            .drivingDistanceKm(drivingDistanceKm)
            .location(Location.builder()
                .latitude(targetGps.getLatitude())
                .longitude(targetGps.getLongitude())
                .build())
            .status(status)
            .build();
    }
}
