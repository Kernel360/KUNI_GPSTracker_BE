package com.example.BackendServer.dashboard.service;

import com.example.BackendServer.dashboard.model.DashboardStatusResponseDto;
import com.example.BackendServer.dashboard.model.DayCountView;
import com.example.BackendServer.record.db.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 어제 포함 과거 7일간의 운행 수를 반환
     *
     * @return [{날짜, 운행 수},...]
     */
    public DashboardStatusResponseDto getDashboardWeekStatus() {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate startDate = yesterday.minusDays(6);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = yesterday.plusDays(1).atStartOfDay();

        List<DayCountView> raw = recordRepository.findDailyCount(start, end);

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
}
