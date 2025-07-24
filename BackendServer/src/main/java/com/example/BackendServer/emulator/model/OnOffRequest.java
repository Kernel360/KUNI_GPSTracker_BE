package com.example.BackendServer.emulator.model;

import lombok.Data;

@Data
public class OnOffRequest {
    private String emulatorId;
    private String timestamp;
}
