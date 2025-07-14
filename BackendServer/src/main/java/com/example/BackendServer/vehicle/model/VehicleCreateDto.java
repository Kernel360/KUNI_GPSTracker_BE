package com.example.BackendServer.vehicle.model;

import com.example.BackendServer.vehicle.db.VehicleEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VehicleCreateDto {

    @NotBlank
    private String vehicleNumber;

    private VehicleEntity.Type vehicleName;
}
