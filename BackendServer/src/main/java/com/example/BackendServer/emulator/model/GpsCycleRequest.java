package com.example.BackendServer.emulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)



public class GpsCycleRequest {
  @JsonProperty("mdn")
  private String mdn; //차량 번호 : sendCycleInfo

  @JsonProperty("tid")
  private String tid; //터미널 아이디

  @JsonProperty("mid")
  private String mid; //제조사 아이디

  @JsonProperty("pv")
  private String pv; //패킷 버전

  @JsonProperty("did")
  private String did; //디바이스 아이디

  @JsonProperty("oTime")
  private LocalDateTime oTime; //발생시간

  @JsonProperty("cCnt")
  private String cCnt; //주기정보 개수

  @JsonProperty("cList")
  private List<GpsCycleData> cList; //주기정보 리스트
}
