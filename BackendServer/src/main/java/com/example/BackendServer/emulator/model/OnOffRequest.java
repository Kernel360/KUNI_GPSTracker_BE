package com.example.BackendServer.emulator.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OnOffRequest {
    private String mdn;
    private String tid;
    private String mid;
    private String pv;
    private String did;
    private String onTime;
    private String offTime;
    private String gcd;
    private String lat;
    private String lon;
    private String ang;
    private String spd;
    private String sum;
}
