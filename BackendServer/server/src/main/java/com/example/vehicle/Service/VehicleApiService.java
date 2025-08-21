package com.example.vehicle.Service;

import com.example.global.Class.VehicleStatus;
import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
import com.example.entity.GpsRecordEntity;
import com.example.repository.GpsRecordRepository;
import com.example.entity.RecordEntity;
import com.example.repository.RecordRepository;
import com.example.entity.VehicleEntity;
import com.example.repository.VehicleRepository;
import com.example.vehicle.model.VehicleCreateDto;
import com.example.vehicle.model.VehicleListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import static com.example.global.Class.VehicleStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleApiService {

    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final GpsRecordRepository gpsRecordRepository;
    private final Random random = new Random();

    @Transactional
    public VehicleEntity createVehicle(VehicleCreateDto dto) {

        String vehicleNumber = dto.getVehicleNumber();

        // 이미 존재하는 차량 번호 체크
        if (vehicleRepository.existsByVehicleNumber(vehicleNumber)) {
            log.warn("차량 등록 실패 - 이미 존재하는 차량 번호: {}", vehicleNumber);
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_EXISTS);
        }

        // 무작위 초기 위치 생성 (한국 지역 기준)
        double initialLatitude = generateRandomLatitude();
        double initialLongitude = generateRandomLongitude();

        VehicleEntity entity = VehicleEntity.builder()
            .vehicleNumber(vehicleNumber)
            .status(INACTIVE)
            .totalDist(0L)
            .type(dto.getVehicleName())
            .createDate(LocalDateTime.now())
            .build();

        VehicleEntity saved = vehicleRepository.save(entity);

        RecordEntity recordEntity = RecordEntity.builder()
            .vehicle(saved)
            .sumDist("0")
            .onTime(LocalDateTime.now())
            .offTime(LocalDateTime.now())
            .build();

        RecordEntity savedRecord = recordRepository.save(recordEntity);

        GpsRecordEntity gpsRecordEntity = GpsRecordEntity.builder()
            .record(savedRecord)
            .vehicle(saved)
            .status(INACTIVE)
            .latitude(initialLatitude)
            .longitude(initialLongitude)
            .oTime(LocalDateTime.now())
            .gcd("V")
            .totalDist("0")
            .build();

        gpsRecordRepository.save(gpsRecordEntity);

        log.info("차량 등록 성공: {}", saved.getVehicleNumber());

        return saved;
    }

    private double generateRandomLatitude() {
        return 33.0 + (random.nextDouble() * 5.5);
    }

    private double generateRandomLongitude() {
        return 124.5 + (random.nextDouble() * 7.5);
    }

    public Page<VehicleListResponse> getVehicleList(Pageable pageable, String vehicleName, VehicleStatus status) {
        return status == null ?
            vehicleRepository.findAllByVehicleNumberContains(vehicleName == null ? "" : vehicleName, pageable)
                .map(VehicleListResponse::from) :
            vehicleRepository.findAllByStatusAndVehicleNumberContains(status, vehicleName == null ? "" : vehicleName,
                pageable).map(VehicleListResponse::from);
    }

    @Transactional
    public void deleteByVehicleNumber(String vehicleNumber) {
        VehicleEntity vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
            .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
        vehicleRepository.delete(vehicle);
        log.info("차량 삭제 완료: {}", vehicleNumber);
    }
}
