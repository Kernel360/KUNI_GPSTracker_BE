package com.example.kafka.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GpsMsg {
    private String id;
    private String type;

    private String mdn;
    private String tid;
    private String mid;
    private String pv;
    private String did;
    private String oTime;
    private String cCnt;
    private List<GpsData> cList;
}
