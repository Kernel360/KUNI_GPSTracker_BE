package com.example.BackendServer.emulator.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MDT {
    private String tid;
    private String mid;
    private String pv;
    private String did;

    public static MDT decidedMDT(){
        return MDT.builder()
                .tid("A001").mid("6").pv("5").did("1")
                .build();
    }
}
