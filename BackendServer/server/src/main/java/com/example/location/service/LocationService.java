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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final GpsRecordRepository gpsRepository;

    /**
     * 차량 번호 기준으로 실시간 위치 조회
     *
     * @param vehicleNumber : 차량 번호
     * @return VehicleRealtimeInfoDto
     */
    public VehicleRealtimeInfoDto getVehicleRealtimeInfo(String vehicleNumber) {

        // 1️⃣ 차량 정보 조회 (status 포함)
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

        // 4️⃣ 2분 전 기준 시각 계산
        LocalDateTime targetTime = LocalDateTime.now().minusMinutes(2);

        // 5️⃣ 2분 전 데이터 선택
        GpsRecordEntity targetGps = gpsList.stream()
            .filter(gps -> !gps.getOTime().isAfter(targetTime)) // 1분 전 이전 데이터
            .max(Comparator.comparing(GpsRecordEntity::getOTime)) // 가장 최근
            .orElseGet(() -> // 없다면 1분 전 ~ 현재 사이에서 가장 가까운 데이터
                gpsList.stream()
                    .filter(gps -> !gps.getOTime().isBefore(targetTime))
                    .min(Comparator.comparing(gps -> Math.abs(Duration.between(gps.getOTime(), targetTime).getSeconds())))
                    .orElse(gpsList.get(0))
            );

        // 6️⃣ 주행 시간 계산
        LocalDateTime endTime = latestRecord.getOffTime();
        if (endTime == null) endTime = LocalDateTime.now();
        long drivingMinutes = Duration.between(latestRecord.getOnTime(), endTime).toMinutes();
        if (drivingMinutes < 0) drivingMinutes = 0;

        // 7️⃣ 주행 거리 계산
        double drivingDistanceKm;
        try {
            drivingDistanceKm = Double.parseDouble(targetGps.getTotalDist());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_RECORD_DURATION);
        }

        // 8️⃣ DTO 반환
        return VehicleRealtimeInfoDto.builder()
            .vehicleNumber(vehicle.getVehicleNumber())
            .vehicleName(vehicle.getType())
            .drivingDate(latestRecord.getOnTime().toLocalDate())
            .drivingTime(drivingMinutes)
            .drivingDistanceKm(drivingDistanceKm)
            .location(Location.builder()
                .latitude(targetGps.getLatitude())
                .longitude(targetGps.getLongitude())
                .build())
            .status(vehicle.getStatus()) // 🚀 차량 상태 포함
            .build();
    }
}
