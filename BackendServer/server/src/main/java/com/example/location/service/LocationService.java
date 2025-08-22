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

        // 4️⃣ 현재 기준 최신 GPS 데이터 확인
        GpsRecordEntity latestGps = gpsList.get(gpsList.size() - 1); // 가장 최근
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(latestGps.getOTime(), now);

        // 5️⃣ 상태 결정: 최근 데이터가 2분 이상 오래되면 INACTIVE
        VehicleStatus status = (diff.toMinutes() >= 2) ? VehicleStatus.INACTIVE : VehicleStatus.ACTIVE;

        // 6️⃣ 2분 전 기준 GPS 선택 (없으면 에러 발생)
        GpsRecordEntity targetGps;

        if (latestRecord.getOffTime() != null) {
            // 🚗 운행 종료 상태라면 → offTime 기준으로 가장 마지막 위치 반환
            targetGps = gpsList.stream()
                    .filter(gps -> !gps.getOTime().isAfter(latestRecord.getOffTime()))
                    .max(Comparator.comparing(GpsRecordEntity::getOTime))
                    .orElse(latestGps); // 만약 없으면 그냥 최신값 사용
        } else {
            // 🚗 운행 중 상태라면 → 기존 로직(2분 전 기준 GPS)
            LocalDateTime targetTime = now.minusMinutes(2);
            targetGps = gpsList.stream()
                    .filter(gps -> !gps.getOTime().isAfter(targetTime))
                    .max(Comparator.comparing(GpsRecordEntity::getOTime))
                    .orElse(latestGps); // 없으면 최신값
        }



        // 7️⃣ 주행 시간 계산
        LocalDateTime endTime = latestRecord.getOffTime();
        if (endTime == null) endTime = now;
        long drivingMinutes = Duration.between(latestRecord.getOnTime(), endTime).toMinutes();
        if (drivingMinutes < 0) drivingMinutes = 0;

        // 8️⃣ 주행 거리 계산
        double drivingDistanceKm;
        try {
            drivingDistanceKm = Double.parseDouble(targetGps.getTotalDist());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_RECORD_DURATION);
        }

        // 9️⃣ 운행 날짜 (LocalDate 타입 그대로)
        LocalDate drivingDate = latestRecord.getOnTime() != null
            ? latestRecord.getOnTime().toLocalDate()
            : null;

        // 🔟 DTO 반환
        return VehicleRealtimeInfoDto.builder()
            .vehicleNumber(vehicle.getVehicleNumber())
            .vehicleName(vehicle.getType())
            .drivingDate(drivingDate)   // ✅ LocalDate 타입
            .drivingTime(drivingMinutes)
            .drivingDistanceKm(drivingDistanceKm)
            .location(Location.builder()
                .latitude(targetGps.getLatitude())
                .longitude(targetGps.getLongitude())
                .build())
            .status(status) // 🚀 상태 반영
            .build();
    }
}