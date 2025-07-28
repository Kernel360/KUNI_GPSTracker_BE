package com.example.BackendServer.device.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
@JsonNaming(value= PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "디바이스 요청 모델")
public class DeviceRequest {
  @Schema(description = "차량 ID", example = "12345")
  private Long vehicleId;
  @Schema(description = "단말기 ID", example = "terminal123")
  private String terminalId;
  @Schema(description = "디바이스 ID", example = "device123")
  private String mid;
  @Schema(description = "디바이스 이름", example = "My Device")
  private String pv;
  @Schema(description = "디바이스 설명", example = "This is a sample device")
  private String did;
}
