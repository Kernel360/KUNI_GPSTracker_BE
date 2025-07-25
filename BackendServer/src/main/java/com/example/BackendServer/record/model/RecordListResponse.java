package com.example.BackendServer.record.model;

import com.example.BackendServer.record.db.RecordEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecordListResponse {
    private Long id;
    private String vehicleNumber;
    private String vehicleName;
    private LocalDateTime onTime;
    private LocalDateTime offTime;
    private String sumDist;

    public static RecordListResponse fromEntity(RecordEntity entity) {
        return RecordListResponse.builder()
                .id(entity.getId())
                .vehicleNumber(entity.getVehicle().getVehicleNumber())
                .vehicleName(entity.getVehicle().getType().name())
                .onTime(entity.getOnTime())
                .offTime(entity.getOffTime())
                .sumDist(entity.getSumDist())
                .build();
    }
}
