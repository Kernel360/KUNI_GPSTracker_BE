package com.example.BackendServer.emulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GpsCycleRequest {
  private String mdn;
  private String tid;
  private String mid;
  private String pv;
  private String did;
  private String oTime;
  private String cCnt;

  @JsonProperty("cList")
  private List<GpsCycleData> cList;

  // token 필드 삭제
}
