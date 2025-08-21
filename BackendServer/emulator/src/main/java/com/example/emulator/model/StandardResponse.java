package com.example.emulator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponse {
  @Schema(description = "결과 코드", example = "000")
  private String rstCd;
  @Schema(description = "결과 메시지", example = "Success")
  private String rstMsg;
  @Schema(description = "차량 번호", example = "12가3456")
  private String mdn;
}