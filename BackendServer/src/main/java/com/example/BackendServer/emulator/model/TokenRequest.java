package com.example.BackendServer.emulator.model;

import lombok.Data;

@Data
public class TokenRequest {
    private String mdn;
    private String tid;
    private String mid;
    private String pv;
    private String did;
    private String dFWVer;
}