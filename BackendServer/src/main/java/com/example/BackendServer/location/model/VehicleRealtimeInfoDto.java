package com.example.BackendServer.location.model;

import com.example.BackendServer.global.Class.VehicleType;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VehicleRealtimeInfoDto {

    private String vehicleNumber;
    private VehicleType vehicleName;
    private LocalDate drivingDate; //2025-07-07
    private Long drivingTime; //1시간 2분
    private Double drivingDistanceKm;
    private Location location;

}
