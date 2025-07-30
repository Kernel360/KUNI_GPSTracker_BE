package com.example.BackendServer.location.model;

import com.example.BackendServer.global.Class.VehicleType;

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
    //TODO : 형식 알아보기
    @Schema(description = "운행 시간", example = "1")
    private Long drivingTime; //1시간 2분
    @Schema(description = "운행 거리 (km)", example = "12.5")
    //TODO : 단위 통합하기, 다른 부분에서 운행 거리가 String으로 되어있음
    private Double drivingDistanceKm;
    @Schema(description = "현재 위치 정보")
    private Location location;

}
