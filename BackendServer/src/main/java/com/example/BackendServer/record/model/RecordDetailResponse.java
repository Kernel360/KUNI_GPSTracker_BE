package com.example.BackendServer.record.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecordDetailResponse {
    private String vehicleNumber;
    private String vehicleName;
    private LocalDateTime onTime;
    private LocalDateTime offTime;
    private String sumDist;
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
}
