package com.example.BackendServer.emulator.model;

import lombok.Data;
import java.util.List;

@Data
public class GpsCycleRequest {
  private String mdn;
  private String tid;
  private String mid;
  private String pv;
  private String did;
  private String oTime;
  private String cCnt;
  private List<GpsCycleData> cList;
}
