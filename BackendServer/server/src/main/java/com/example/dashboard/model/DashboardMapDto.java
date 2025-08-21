package com.example.dashboard.model;

import com.example.model.VehicleStatus;
import com.example.model.VehicleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "대시보드 차량 위치 모델")
public class DashboardMapDto {
    @Schema(description = "차량 위도", example = "37.111111")
    private double latitude;
    @Schema(description = "차량 경도", example = "123.456789")
    private double longitude;
    @Schema(description = "차량 상태", example = "ACTIVE")
    private VehicleStatus status;
    @Schema(description = "차량 번호", example = "12가3456")
    private String vehicleNumber;
    @Schema(description = "차량 종류", example = "MERCEDES")
    private VehicleType type;
    @Schema(description = "데이터 조회 시간 (1분 전 기준)", example = "2025-01-27T10:29:00")
    private LocalDateTime dataRetrievedAt;
}
