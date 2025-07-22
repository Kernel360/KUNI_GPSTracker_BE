package com.example.car_emulator_server.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private String rstCd;
    private String rstMsg;
    private String mdn;
    private String token;
    private String exPeriod;
}
