package com.example.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

import static lombok.AccessLevel.PROTECTED;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Builder(toBuilder = true)
@Schema(description = "대시보드 주간 운행 통계 모델")
public class DashboardStatusResponseDto {

    @Schema(description = "1주간 운행 통계", example = """
      [
        {"day":"2025-07-14","totalCar":100},
        {"day":"2025-07-15","totalCar":200},
        {"day":"2025-07-16","totalCar":300},
        {"day":"2025-07-17","totalCar":400},
        {"day":"2025-07-18","totalCar":500},
        {"day":"2025-07-19","totalCar":600},
        {"day":"2025-07-20","totalCar":701}
      ]
    """)
    private DayCount[] dayCount;


    // TODO : 외부로 빼버리기
    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class DayCount {
        private LocalDate day;
        private long totalCar;
    }
}
