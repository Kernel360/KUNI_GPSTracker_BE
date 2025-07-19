package com.example.BackendServer.gpsRecord.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GpsRecordRequest {

  private Long vehicleId;
  private Long driverId;
  private String status; // ACTIVE, INACTIVE, INSPECTING
  private Double latitude;
  private Double longitude;
  private LocalDateTime oTime;
  private String gcd;
  private String totalDist;
}
