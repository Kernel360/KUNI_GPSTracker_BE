package com.example.model;

import com.example.entity.RecordEntity;
import com.example.global.Class.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "운행일지 목록 응답 DTO")
public class RecordListResponse {
    @Schema(description = "운행일지 ID", example = "1")
    private Long id;
    @Schema(description = "차량 번호", example = "123가4567")
    private String vehicleNumber;
    @Schema(description = "차량 종류", example = "AVANTE")
    private VehicleType vehicleName;
    @Schema(description = "운행 시작 시간", example = "2023-10-01T08:00:00")
    private LocalDateTime onTime;
    @Schema(description = "운행 종료 시간", example = "2023-10-01T18:00:00")
    private LocalDateTime offTime;
    @Schema(description = "총 주행 거리", example = "150.5")
    private String sumDist;

    public static RecordListResponse from(RecordEntity entity) {
        return RecordListResponse.builder()
                .id(entity.getId())
                .vehicleNumber(entity.getVehicle().getVehicleNumber())
                .vehicleName(entity.getVehicle().getType())
                .onTime(entity.getOnTime())
                .offTime(entity.getOffTime())
                .sumDist(entity.getSumDist())
                .build();
    }
}
