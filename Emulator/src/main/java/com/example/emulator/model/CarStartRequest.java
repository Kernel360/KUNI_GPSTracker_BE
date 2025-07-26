package com.example.emulator.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarStartRequest {
    private String mdn;
    private MDT mdt;
    private String onTime;
    private String offTime;
    private Gps gps;

}
