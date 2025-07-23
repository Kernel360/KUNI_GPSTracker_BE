package com.example.car_emulator_server.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarStopRequest {
    private String mdn;
    private MDT mdt;
    private String onTime;
    private String offTime;
    private Gps gps;

}
