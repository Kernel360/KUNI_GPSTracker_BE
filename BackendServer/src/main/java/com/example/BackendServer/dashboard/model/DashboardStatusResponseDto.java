package com.example.BackendServer.dashboard.model;

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

    @Schema(description = "1주간 운행 통계", example = "1, 320")
    private DayCount[] dayCount;

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
