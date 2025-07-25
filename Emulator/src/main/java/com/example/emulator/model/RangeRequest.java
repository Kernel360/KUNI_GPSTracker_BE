package com.example.emulator.model;

import lombok.Data;

@Data
public class RangeRequest {
    private int start;
    private int end;
    private int interval;
}
