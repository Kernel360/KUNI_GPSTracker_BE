package com.example.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopVehicleResponseDto {
    private String vehicleNumber;
    private Long driveCount;
}
