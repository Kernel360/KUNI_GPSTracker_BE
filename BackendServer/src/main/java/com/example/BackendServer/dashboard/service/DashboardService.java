package com.example.BackendServer.dashboard.service;

import com.example.BackendServer.dashboard.model.DashboardStatusResponseDto;
import com.example.BackendServer.dashboard.model.DayCountView;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.BackendServer.dashboard.model.DashboardResponseDto;
import com.example.BackendServer.dashboard.model.DashboardMapDto;
import com.example.BackendServer.gpsRecord.db.GpsRecordRepository;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final RecordRepository recordRepository;
    private final VehicleRepository vehicleRepository;
    private final GpsRecordRepository gpsRecordRepository;

    /**
     * 어제 포함 과거 7일간의 운행 수를 반환
     *
     * @return [{날짜, 운행 수},...]
     */
    public DashboardStatusResponseDto getDashboardWeekStatus() {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate startDate = yesterday.minusDays(6);

        List<DayCountView> raw = recordRepository.findDailyCount(startDate, yesterday);

        Map<LocalDate, Long> countMap = raw.stream()
                .collect(Collectors.toMap(DayCountView::getDay,
                        DayCountView::getTotalCar));

        DashboardStatusResponseDto.DayCount[] dayCounts = new DashboardStatusResponseDto.DayCount[7];
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            long cnt = countMap.getOrDefault(date, 0L);
            dayCounts[i] = DashboardStatusResponseDto.DayCount.builder()
                    .day(date)
                    .totalCar(cnt)
                    .build();
        }

        return DashboardStatusResponseDto.builder()
                .dayCount(dayCounts)
                .build();
    }


    //기존 dashboard api 전체 차량 개수와 status에 따른 각 개수를 반환한다
    public DashboardResponseDto getDashboardData() {
        long total = vehicleRepository.count();
        long active = vehicleRepository.countByStatus(VehicleEntity.Status.ACTIVE);
        long inactive = vehicleRepository.countByStatus(VehicleEntity.Status.INACTIVE);
        long inspect = vehicleRepository.countByStatus(VehicleEntity.Status.INSPECTING);

        return DashboardResponseDto.builder()
                .vehicles(total)
                .active(active)
                .inactive(inactive)
                .inspect(inspect)
                .build();
    }

    //map api로 vehicle_id에 따라 위도 경도 상태 가져오는 리스트 반환 함수 (status가 null 이면 전체 또는 각 상태를 지정해서 필터링해서 가져온다
    //여기서의 record는 GPSRecordEntity의 record와는 아예 다른 것이다
    public List<DashboardMapDto> getAllVehicleLocation(String status) {
        List<GpsRecordEntity> latestRecords = gpsRecordRepository.findLatestGpsForAllVehiclesByStatus(status);

        return latestRecords.stream()
            .map(record -> DashboardMapDto.builder()
                    .latitude(record.getLatitude())
                    .longitude(record.getLongitude())
                    .status(record.getStatus().name().toLowerCase())
                    .type(record.getVehicle().getType().name())
                    .vehicleNumber(record.getVehicle().getVehicleNumber())
                    .build())
            .collect(Collectors.toList());
    }
}
