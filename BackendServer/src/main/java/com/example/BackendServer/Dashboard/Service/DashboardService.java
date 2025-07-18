package com.example.BackendServer.Dashboard.Service;

import com.example.BackendServer.Dashboard.model.DashboardResponseDto;
import com.example.BackendServer.Dashboard.model.DashboardMapDto;
import com.example.BackendServer.gpsRecord.db.GpsRecordRepository;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;


@Service
@RequiredArgsConstructor
public class DashboardService {
    private final VehicleRepository vehicleRepository;
    private final GpsRecordRepository gpsRecordRepository;

    //기존 dashboard api 전체 차량 개수와 status에 따른 각 개수를 반환한다
    public DashboardResponseDto getDashboardData() {
        long total = vehicleRepository.count();
        long active = vehicleRepository.countByStatus(VehicleEntity.Status.ACTIVE);
        long inactive = vehicleRepository.countByStatus(VehicleEntity.Status.INACTIVE);
        long inspect = vehicleRepository.countByStatus(VehicleEntity.Status.INSPECTING);

        return DashboardResponseDto.builder()
                .vehicles((int) total)
                .active((int) active)
                .inactive((int) inactive)
                .inspect((int) inspect)
                .build();
    }

    //map api로 vehicle_id에 따라 위도 경도 상태 가져오는 리스트 반환 함수 (status가 null 이면 전체 또는 각 상태를 지정해서 필터링해서 가져온다
    public List<DashboardMapDto> getAllVehicleLocation(String status) {
        List<GpsRecordEntity> latestRecords = gpsRecordRepository.findLatestGpsForAllVehicles();

        return latestRecords.stream()
            .filter(record -> status == null || record.getStatus().name().equalsIgnoreCase(status))
            .map(record -> DashboardMapDto.builder()
                    .latitude(record.getLatitude())
                    .longitude(record.getLongitude())
                    .status(record.getStatus().name().toLowerCase())
                    .build())
            .collect(Collectors.toList());
    }
}
