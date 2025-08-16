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
import java.time.LocalDateTime;
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

    //map api로 vehicle_number에 따라 위도 경도 상태 가져오는 리스트 반환 함수
    //차량 번호 리스트가 null이면 전체 차량, 아니면 지정된 차량들의 1분 전 위치 정보를 반환
    public List<DashboardMapDto> getAllVehicleLocation(List<String> vehicleNumbers) {
        // 요청한 시간 기준으로 1분 전 GPS 데이터를 조회
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(2);

        List<GpsRecordEntity> latestRecords;

        if (vehicleNumbers == null || vehicleNumbers.isEmpty()) {
            // 차량 번호가 null이면 전체 차량의 1분 전 위치 정보 조회
            latestRecords = gpsRecordRepository.findLatestGpsForAllVehiclesByTime(oneMinuteAgo);
        } else {
            // 지정된 차량 번호들의 1분 전 위치 정보 조회
            latestRecords = gpsRecordRepository.findLatestGpsByVehicleNumbersAndTime(vehicleNumbers, oneMinuteAgo);
        }

        return latestRecords.stream()
                .map(record -> DashboardMapDto.builder()
                        .latitude(record.getLatitude())
                        .longitude(record.getLongitude())
                        .status(record.getStatus())
                        .vehicleNumber(record.getVehicle().getVehicleNumber())
                        .type(record.getVehicle().getType()) // 차량 종류 (MERCEDES, FERRARI, PORSCHE)
                        .dataRetrievedAt(record.getOTime())
                        .build())
                .collect(Collectors.toList());
    }

}
