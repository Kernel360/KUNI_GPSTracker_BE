package com.example.BackendServer.emulator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor  // 이걸 꼭 추가하세요
public class TokenResponse {
    private String rstCd;
    private String rstMsg;
    private String mdn;
    private String token;
    private String exPeriod;
}
