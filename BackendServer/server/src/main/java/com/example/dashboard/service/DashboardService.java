package com.example.dashboard.service;

import com.example.dashboard.model.DashboardMapDto;
import com.example.dashboard.model.DashboardResponseDto;
import com.example.dashboard.model.DashboardStatusResponseDto;
import com.example.entity.GpsRecordEntity;
import com.example.model.DayCountView;
import com.example.repository.GpsRecordRepository;
import com.example.repository.RecordRepository;
import com.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.dashboard.model.TopVehicleResponseDto;
import com.example.entity.VehicleEntity;


import com.example.model.VehicleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final RecordRepository recordRepository;
    private final VehicleRepository vehicleRepository;
    private final GpsRecordRepository gpsRecordRepository;

    private static final int TOP_VEHICLES_LIMIT = 3;

    /**
     * 최근 1주일간 운행량이 가장 많은 차량 TOP 3 반환
     *
     * @return [{vehicleNumber, driveCount}, ...]
     */
    public List<TopVehicleResponseDto> getWeeklyTopVehicles() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        List<Object[]> results = recordRepository.findTopVehicles(oneWeekAgo);

        return results.stream()
                .limit(TOP_VEHICLES_LIMIT)
                .map(obj -> {
                    VehicleEntity vehicle = (VehicleEntity) obj[0];
                    Long count = (Long) obj[1];
                    return new TopVehicleResponseDto(vehicle.getVehicleNumber(), count);
                })
                .collect(Collectors.toList());
    }

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
        long active = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
        long inactive = vehicleRepository.countByStatus(VehicleStatus.INACTIVE);
        long inspect = vehicleRepository.countByStatus(VehicleStatus.INSPECTING);

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

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime twoMinAgo = now.minusMinutes(2);

        List<GpsRecordEntity> latestMinus2 = gpsRecordRepository.findLatestGpsForAllVehiclesNULL(twoMinAgo);
        List<GpsRecordEntity> latestNow    = gpsRecordRepository.findLatestGpsForAllVehiclesNULL(now);

        // vehicleNumbers == null ➜ 전체, null이 아니면(비어 있어도) 지정된 목록만
        final Set<String> filterSet = (vehicleNumbers == null) ? null : Set.copyOf(vehicleNumbers);

        Predicate<GpsRecordEntity> vehicleFilter =
                (filterSet == null)
                        ? r -> true
                        : r -> filterSet.contains(r.getVehicle().getVehicleNumber());

        Function<GpsRecordEntity, DashboardMapDto> toDto = r -> DashboardMapDto.builder()
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .status(r.getVehicle().getStatus())
                .vehicleNumber(r.getVehicle().getVehicleNumber())
                .type(r.getVehicle().getType())
                .dataRetrievedAt(r.getOTime())
                .build();

        return Stream.concat(
                latestMinus2.stream()
                        .filter(r -> r.getVehicle().getStatus() == ACTIVE)
                        .filter(vehicleFilter)
                        .map(toDto),
                latestNow.stream()
                        .filter(r -> r.getVehicle().getStatus() == INACTIVE)
                        .filter(vehicleFilter)
                        .map(toDto)
        ).toList();
    }
}