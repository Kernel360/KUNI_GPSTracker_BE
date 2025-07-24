package com.example.BackendServer.emulator.model;

import lombok.Data;

@Data
public class TokenRequest {
    private String emulatorId;
    private String secret;
}