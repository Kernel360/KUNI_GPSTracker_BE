package com.example.BackendServer.emulator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TokenRequest {
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
    @Schema(description = "디바이스 펌웨어 버전", example = "LTE1.2")
    private String dFWVer;
}