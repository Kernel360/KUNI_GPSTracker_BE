package com.example.BackendServer.emulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class GpsCycleData {
    @JsonProperty("sec")
    @Schema(description = "발생시간 ‘초’", example = "33")
    private String sec; //발생시간 '초' : sendCycleInfo

    @JsonProperty("gcd")
    @Schema(description = "GPS 상태", example = "A")
    private String gcd; //GPS 상태

    @JsonProperty("lat")
    @Schema(description = "GPS 위도", example = "36123456")
    private String lat; //GPS 위도

    @JsonProperty("lon")
    @Schema(description = "GPS 경도", example = "127123456")
    private String lon; //GPS 경도

    @JsonProperty("ang")
    @Schema(description = "방향", example = "270")
    private String ang; //방향

    @JsonProperty("spd")
    @Schema(description = "속도", example = "5")
    private String spd; //속도

    @JsonProperty("sum")
    @Schema(description = "누적 주행 거리", example = "10000")
    private String sum; //누적 주행 거리

    @JsonProperty("bat")
    @Schema(description = "배터리 전압", example = "100")
    private String bat; //배터리 전압


}

