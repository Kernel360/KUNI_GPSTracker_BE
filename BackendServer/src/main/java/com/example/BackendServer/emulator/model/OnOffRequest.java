package com.example.BackendServer.emulator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OnOffRequest {
    @Schema(description = "차량 번호", example = "12가3456")
    private String mdn;
    @Schema(description = "터미널 아이디", example = "A001")
    private String tid;
    @Schema(description = "제조사 아이디", example = "6")
    private String mid;
    @Schema(description = "패킷 버전", example = "5")
    private String pv;
    @Schema(description = "디바이스 아이디", example = "1")
    private String did;
    @Schema(description = "차량 시동 On 시간", example = "20210901092000")
    private String onTime;
    @Schema(description = "차량 시동 Off 시간", example = "20210901092000")
    private String offTime;
    @Schema(description = "GPS 상태", example = "A")
    private String gcd;
    @Schema(description = "GPS 위도", example = "36123456")
    private String lat;
    @Schema(description = "GPS 경도", example = "127123456")
    private String lon;
    @Schema(description = "방향", example = "270")
    private String ang;
    @Schema(description = "속도", example = "5")
    private String spd;
    @Schema(description = "누적 주행 거리", example = "10000")
    private String sum;
}
