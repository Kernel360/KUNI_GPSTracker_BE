package com.example.BackendServer.vehicle.model;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VehicleCreateDto {

    private String vehicleNumber;
    private String vehicleName;
}
