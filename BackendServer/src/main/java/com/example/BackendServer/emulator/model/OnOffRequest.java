package com.example.BackendServer.emulator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OnOffRequest {
    private String mdn;
    private String tid;
    private String mid;
    private String pv;
    private String did;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime onTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime offTime;

    private String gcd;
    private double lat;
    private double lon;
    private int ang;
    private int spd;
    private int sum;

    // token 필드 삭제
}
