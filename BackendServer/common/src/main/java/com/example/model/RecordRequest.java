package com.example.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "운행일지 생성 요청 DTO")
public class RecordRequest {
  @Schema(description = "차량 ID", example = "123가4567")
  private Long vehicleId;
  @Schema(description = "총 주행 거리", example = "150.5")
  private String sumDist;
  @Schema(description = "운행 시작 시간", example = "2023-10-01T08:00:00")
  private LocalDateTime onTime;
  // TODO : 운행 종료 시간은 없어도 될거 같습니다.
  @Schema(description = "운행 종료 시간", example = "2023-10-01T18:00:00", nullable = true)
  private LocalDateTime offTime;
}
