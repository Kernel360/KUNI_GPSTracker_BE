package com.example.BackendServer.emulator.model;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OnOffRequest {
    private String mdn; // 차량번호 : SendCartStart
    private String tid; // 터미널 아이디
    private String mid; // 제조사 아이디
    private String pv; //패킷 버전
    private String did; //디바이스 아이디
    private LocalDateTime onTime; //차량 시동 On 시간
    private LocalDateTime offTime; //차량 시동 Off 시간
    private String gcd; //GPS 상태
    private String lat; //GPS 위도
    private String lon; //GPS 경도
    private String ang; //방향
    private String spd; //속도
    private String sum; //누적 주행 거리
}
