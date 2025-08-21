package com.example.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "운행일지 GPS 기록 응답 DTO")
public class RecordPointResponse {
    @Schema(description = "GPS 기록 시간", example = "2023-10-01T08:00:00")
    private LocalDateTime time;
    @Schema(description = "GPS 기록 위도", example = "37.5665")
    private Double lat;
    @Schema(description = "GPS 기록 경도", example = "126.9780")
    private Double lng;

    public static RecordPointResponse from(com.example.entity.GpsRecordEntity gps) {
        return RecordPointResponse.builder()
                .time(gps.getOTime())
                .lat(gps.getLatitude())
                .lng(gps.getLongitude())
                .build();
    }
}
