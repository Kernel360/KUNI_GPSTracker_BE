package com.example.BackendServer.emulator.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GpsCycleData {
    private String sec;
    private String gcd;
    private String lat;
    private String lon;
    private String ang;
    private String spd;
    private String sum;
    private String bat;
}

