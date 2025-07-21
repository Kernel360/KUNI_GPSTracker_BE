package com.example.BackendServer.location.model;

import com.example.BackendServer.vehicle.db.VehicleEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VehicleRealtimeInfoDto {

    private String vehicleNumber;
    private VehicleEntity.Type vehicleName;
    private LocalDate drivingDate; //2025-07-07
    private Long drivingTime; //1시간 2분
    private Double drivingDistanceKm;
    private Location location;

}
