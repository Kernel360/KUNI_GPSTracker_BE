package com.example.kafka.model;

import lombok.*;


@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GpsData {
    private Integer sec; //발생시간 '초' : sendCycleInfo
    private String gcd; //GPS 상태
    private String lat; //GPS 위도
    private String lon; //GPS 경도
    private String ang; //방향
    private String spd; //속도
    private String sum; //누적 주행 거리
    private String bat; //배터리 전압
}

