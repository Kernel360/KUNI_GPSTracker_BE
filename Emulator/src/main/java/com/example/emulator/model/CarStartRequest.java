package com.example.emulator.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarStartRequest {
    private String mdn;
    @JsonUnwrapped
    private MDT mdt;
    private String onTime;
    private String offTime;
    @JsonUnwrapped
    private Gps gps;

}
