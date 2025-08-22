package com.example.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopVehicleResponseDto {

    @Schema(description = "차량 번호", example = "12가3456")
    private String vehicleNumber;

    @Schema(description = "최근 1주일간 운행 횟수", example = "15")
    private Long driveCount;
}
