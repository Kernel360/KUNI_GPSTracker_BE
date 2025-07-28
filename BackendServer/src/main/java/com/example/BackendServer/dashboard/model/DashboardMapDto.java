package com.example.BackendServer.dashboard.model;

import com.example.BackendServer.global.Class.VehicleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

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
}
