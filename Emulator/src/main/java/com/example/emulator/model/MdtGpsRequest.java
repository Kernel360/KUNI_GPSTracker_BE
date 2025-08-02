package com.example.emulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MdtGpsRequest {

    private String mdn;
    @JsonUnwrapped
    private MDT mdt;
    @JsonProperty("oTime")
    private String oTime;
    @JsonProperty("cCnt")
    private String cCnt;
    @JsonProperty("cList")
    private List<CList> cList;

    @Data
    @Builder
    public static class CList{
        private String sec;
        @JsonUnwrapped
        private Gps gps;
        private String bat;
    }
}
