package com.example.BackendServer.emulator.model;

import lombok.Data;

@Data
public class GpsRequest {
    private String emulatorId;
    private double latitude;
    private double longitude;
    private String timestamp;
}
