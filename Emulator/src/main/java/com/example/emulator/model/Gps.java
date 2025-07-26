package com.example.emulator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class Gps {
    private String gcd;
    private String lon;
    private String lat;
    private String spd;
    private String ang;
    private String sum;

}
