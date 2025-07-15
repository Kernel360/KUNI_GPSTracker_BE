package com.example.BackendServer.vehicle.model;

import com.example.BackendServer.vehicle.db.VehicleEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "차량 생성 요청 DTO")
public class VehicleCreateDto {

    @NotBlank
    @Schema(description = "차량 번호", example = "12가3456")
    private String vehicleNumber;
    @Schema(description = "차량 이름", example = "MERCEDES")
    private VehicleEntity.Type vehicleName;
}
