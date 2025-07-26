package com.example.emulator.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TokenHolder {
    private String token;
    private Instant exPeriod;

    public boolean isExpired(){
        return exPeriod.isBefore(Instant.now());
    }
}
