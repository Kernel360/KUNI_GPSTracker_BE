package com.example.BackendServer.record.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecordPointResponse {
    private LocalDateTime time;
    private Double lat;
    private Double lng;

    public static RecordPointResponse fromEntity(com.example.BackendServer.gpsRecord.db.GpsRecordEntity gps) {
        return RecordPointResponse.builder()
                .time(gps.getOTime())
                .lat(gps.getLatitude())
                .lng(gps.getLongitude())
                .build();
    }
}
