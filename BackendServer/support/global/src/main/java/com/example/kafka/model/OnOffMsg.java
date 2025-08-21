package com.example.kafka.model;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OnOffMsg {
    private String id;
    private String type;
    private String mdn;
    private String tid;
    private String mid;
    private String pv;
    private String did;
    private String onTime;
    private String offTime;
    private String gcd;
    private String lat;
    private String lon;
    private String ang;
    private String spd;
    private String sum;
}
