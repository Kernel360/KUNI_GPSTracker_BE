package com.example.gpsRecord.model;

import com.example.global.Class.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
@Schema(description = "GPS 레코드 요청 모델")
public class GpsRecordRequest {
  @Schema(description = "차량 ID", example = "12345")
  private Long vehicleId;
  @Schema(description = "차량 상태", example = "ACTIVE")
  private VehicleStatus status;
  @Schema(description = "위도", example = "37.5665")
  private Double latitude;
  @Schema(description = "경도", example = "126.978")
  private Double longitude;
  // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
  @Schema(description = "GPS 기록 시간", example = "2023-10-01 12:00:00")
  private LocalDateTime oTime;
  @Schema(description = "속도", example = "60")
  private String gcd;
  @Schema(description = "운전 거리", example = "90")
  private String totalDist;
}
