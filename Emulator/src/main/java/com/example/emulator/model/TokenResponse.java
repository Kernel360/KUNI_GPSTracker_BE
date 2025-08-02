package com.example.emulator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TokenResponse {

    private String rstCd;
    private String rstMsg;
    private String mdn;
    private String token;
    private String exPeriod;
}
