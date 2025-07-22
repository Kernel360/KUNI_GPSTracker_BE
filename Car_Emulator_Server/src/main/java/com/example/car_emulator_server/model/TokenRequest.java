package com.example.car_emulator_server.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRequest {
    private String mdn;
    private String dFWVer;

    private MDT mdt;

}
