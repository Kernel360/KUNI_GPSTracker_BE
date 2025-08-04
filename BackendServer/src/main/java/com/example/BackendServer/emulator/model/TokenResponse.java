package com.example.BackendServer.emulator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor  // 이걸 꼭 추가하세요
public class TokenResponse {
    @Schema(description = "결과 코드", example = "000")
    private String rstCd;
    @Schema(description = "결과 메시지", example = "Success")
    private String rstMsg;
    @Schema(description = "차량 번호", example = "12가3456")
    private String mdn;
    @Schema(description = "단말 인증 토큰 값", example = "ajksdjfias2sdkajsREdasudjh354")
    private String token;
    @Schema(description = "토큰 만료 기한(시간 단위)", example = "4")
    private String exPeriod;
}
