package com.example.BackendServer.gpsRecord.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GpsRecordRequest {

  private Long vehicleId;
  private String status; // ACTIVE, INACTIVE, INSPECTING
  private Double latitude;
  private Double longitude;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
  private LocalDateTime oTime;
  private String gcd;
  private String totalDist;
}
