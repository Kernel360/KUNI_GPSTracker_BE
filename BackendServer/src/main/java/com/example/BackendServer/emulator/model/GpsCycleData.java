package com.example.BackendServer.emulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String sec; //발생시간 '초' : sendCycleInfo

    @JsonProperty("gcd")
    private String gcd; //GPS 상태

    @JsonProperty("lat")
    private String lat; //GPS 위도

    @JsonProperty("lon")
    private String lon; //GPS 경도

    @JsonProperty("ang")
    private String ang; //방향

    @JsonProperty("spd")
    private String spd; //속도

    @JsonProperty("sum")
    private String sum; //누적 주행 거리

    @JsonProperty("bat")
    private String bat; //배터리 전압
}

