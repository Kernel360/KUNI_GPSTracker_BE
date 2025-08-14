package com.example.dashboard.service;

import com.example.dashboard.model.DashboardMapDto;
import com.example.dashboard.model.DashboardResponseDto;
import com.example.dashboard.model.DashboardStatusResponseDto;
import com.example.global.Class.VehicleStatus;
import com.example.entity.GpsRecordEntity;
import com.example.model.DayCountView;
import com.example.repository.GpsRecordRepository;
import com.example.repository.RecordRepository;
import com.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.global.Class.VehicleStatus.*;


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
        long active = vehicleRepository.countByStatus(ACTIVE);
        long inactive = vehicleRepository.countByStatus(INACTIVE);
        long inspect = vehicleRepository.countByStatus(INSPECTING);

        return DashboardResponseDto.builder()
                .vehicles(total)
                .active(active)
                .inactive(inactive)
                .inspect(inspect)
                .build();
    }

    //map api로 vehicle_id에 따라 위도 경도 상태 가져오는 리스트 반환 함수 (status가 null 이면 전체 또는 각 상태를 지정해서 필터링해서 가져온다
    //여기서의 record는 GPSRecordEntity의 record와는 아예 다른 것이다
    public List<DashboardMapDto> getAllVehicleLocation(VehicleStatus status) {
        List<GpsRecordEntity> latestRecords = gpsRecordRepository.findLatestGpsForAllVehiclesByStatus(status);

        return latestRecords.stream()
                .map(record -> DashboardMapDto.builder()
                        .latitude(record.getLatitude())
                        .longitude(record.getLongitude())
                        .status(record.getStatus())
                        .vehicleNumber(record.getVehicle().getVehicleNumber())
                        .type(record.getVehicle().getType()) // 차량 종류 (MERCEDES, FERRARI, PORSCHE)
                        .build())
                .collect(Collectors.toList());
    }

}
