package com.example.BackendServer.device.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor  // 이걸 추가해야 builder에서 사용할 생성자가 생깁니다.
@Getter
@ToString
@Builder
@JsonNaming(value= PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeviceRequest {
  private Long vehicleId;
  private String terminalId;
  private String mid;
  private String pv;
  private String did;

}
