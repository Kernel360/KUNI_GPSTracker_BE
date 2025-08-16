package com.example.location.model;

import com.example.global.Class.VehicleStatus;
import com.example.global.Class.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "차량 실시간 정보 DTO")
public class VehicleRealtimeInfoDto {
    @Schema(description = "차량 번호", example = "12가 3456")
    private String vehicleNumber;

    @Schema(description = "차량 종류", example = "AVANTE")
    private VehicleType vehicleName;

    @Schema(description = "운행 날짜", example = "2025-07-07")
    private LocalDate drivingDate;

    @Schema(description = "운행 시간(분)", example = "62")
    private Long drivingTime;

    @Schema(description = "운행 거리 (km)", example = "12.5")
    private Double drivingDistanceKm;

    @Schema(description = "현재 위치 정보")
    private Location location;

    @Schema(description = "차량 상태", example = "ACTIVE")
    private VehicleStatus status;  // 🚀 추가: 차량 상태
}
