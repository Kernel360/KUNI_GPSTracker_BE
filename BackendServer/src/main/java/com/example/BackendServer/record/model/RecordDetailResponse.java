package com.example.BackendServer.record.model;

import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.record.db.RecordEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<RecordPointResponse> record;

    public static RecordDetailResponse fromEntity(RecordEntity entity,
                                                  Double startLat,
                                                  Double startLng,
                                                  Double endLat,
                                                  Double endLng,
                                                  List<GpsRecordEntity> gpsList) {

        return RecordDetailResponse.builder()
                .vehicleNumber(entity.getVehicle().getVehicleNumber())
                .vehicleName(entity.getVehicle().getType().name())
                .onTime(entity.getOnTime())
                .offTime(entity.getOffTime())
                .sumDist(entity.getSumDist())
                .startLat(startLat)
                .startLng(startLng)
                .endLat(endLat)
                .endLng(endLng)
                .record(gpsList.stream()
                        .map(RecordPointResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
