package com.example.car_emulator_server.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MdtGpsRequest {

    private String mdn;
    private MDT mdt;
    private String oTime;
    private String cCnt;
    private List<CList> cList;

    @Data
    @Builder
    public static class CList{
        private Gps gps;
        private String sec;
        private String bat;
    }
}
