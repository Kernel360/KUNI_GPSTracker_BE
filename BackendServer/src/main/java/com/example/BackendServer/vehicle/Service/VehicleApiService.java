package com.example.BackendServer.vehicle.Service;

import static com.example.BackendServer.global.Class.VehicleStatus.*;

import com.example.BackendServer.global.Class.VehicleStatus;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import com.example.BackendServer.vehicle.model.VehicleListResponse;
import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.gpsRecord.db.GpsRecordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleApiService {

    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final GpsRecordRepository gpsRecordRepository;
    private final Random random = new Random();

    /**
     * 차량 등록 : Repository에 차량 entity를 등록한다.
     * 차량 등록 후 RecordEntity와 GpsRecordEntity에 초기 데이터를 생성한다.
     *
     * @param dto : vehicleNumber와 vehicleName을 field로 가진다.
     * @return VehicleEntity  : dto를 이용해 Builder로 Entity 생성 후 반환
     */
	  @Transactional
    public VehicleEntity createVehicle(VehicleCreateDto dto) {

        // 무작위 초기 위치 생성 (한국 지역 기준)
        double initialLatitude = generateRandomLatitude();
        double initialLongitude = generateRandomLongitude();

        VehicleEntity entity = VehicleEntity.builder()
                .vehicleNumber(dto.getVehicleNumber())
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
                .offTime(null)
                .build();
        
        RecordEntity savedRecord = recordRepository.save(recordEntity);
        
        GpsRecordEntity gpsRecordEntity = GpsRecordEntity.builder()
                .record(savedRecord)
                .vehicle(saved)
                .status(VehicleStatus.INACTIVE)
                .latitude(initialLatitude)
                .longitude(initialLongitude)
                .oTime(LocalDateTime.now()) 
                .gcd("V") 
                .totalDist("0")
                .build();
        
        gpsRecordRepository.save(gpsRecordEntity);
        
        return saved;
    }

    
    //무작위 위도 생성 (한국 지역: 33.0 ~ 38.5)
    private double generateRandomLatitude() {
        return 33.0 + (random.nextDouble() * 5.5);
    }

    
    //무작위 경도 생성 (한국 지역: 124.5 ~ 132.0)
    private double generateRandomLongitude() {
        return 124.5 + (random.nextDouble() * 7.5);
    }

    /**
     * 차량 리스트 조회 : 차량 타입과 상태에 따라 페이지네이션된 차량 리스트를 반환한다.
     * @param pageable page, sort 정보를 가진다정
     * @param vehicleName 차량 번호
     * @param status 차량 상태
     * @return Page<VehicleListResponse> : 차량 리스트를 반환한다.
     */
    public Page<VehicleListResponse> getVehicleList(Pageable pageable, String vehicleName, VehicleStatus status) {
        // TODO: status가 ALL일 경우 모든 상태의 차량을 조회하도록 수정 필요
        return vehicleRepository.findAllByStatusAndVehicleNumberContains(status, vehicleName == null ? "" : vehicleName,
            pageable).map(vehicle -> VehicleListResponse.builder()
            .carNumber(vehicle.getVehicleNumber())
            .type(vehicle.getType())
            .status(vehicle.getStatus())
            .totalDist(vehicle.getTotalDist())
            .build()
        );
    }

    // 삭제
    @Transactional
    public void deleteByVehicleNumber(String vehicleNumber) {
        VehicleEntity vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new IllegalArgumentException(vehicleNumber));
        vehicleRepository.delete(vehicle);
    }
}
