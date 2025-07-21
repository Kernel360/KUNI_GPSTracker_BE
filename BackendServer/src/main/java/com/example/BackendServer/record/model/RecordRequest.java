package com.example.BackendServer.record.model;

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
public class RecordRequest {

  private Long vehicleId;
  private String sumDist;
  private LocalDateTime onTime;
  private LocalDateTime offTime;
}
