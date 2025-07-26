package com.example.BackendServer.emulator.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GpsCycleData {
    private String sec;
    private String gcd;
    private double lat;
    private double lon;
    private int ang;
    private int spd;
    private int sum;
    private int bat;
}


