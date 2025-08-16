package com.example.emulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)



public class GpsCycleRequest {

  @JsonProperty("mdn")
  @Schema(description = "차량 번호", example = "12가3456")
  private String mdn; //차량 번호 : sendCycleInfo

  @JsonProperty("tid")
  @Schema(description = "터미널 아이디", example = "A001")
  private String tid; //터미널 아이디

  @JsonProperty("mid")
  @Schema(description = "제조사 아이디", example = "6")
  private String mid; //제조사 아이디

  @JsonProperty("pv")
  @Schema(description = "패킷 버전", example = "5")
  private String pv; //패킷 버전

  @JsonProperty("did")
  @Schema(description = "디바이스 아이디", example = "1")
  private String did; //디바이스 아이디

  @JsonProperty("oTime")
  @Schema(description = "발생시간", example = "202109010920")
  private String oTime; //발생시간

  @JsonProperty("cCnt")
  @Schema(description = "주기정보 개수", example = "10")
  private String cCnt; //주기정보 개수

  @JsonProperty("cList")
  @Schema(description = "주기정보 리스트")
  private List<GpsCycleData> cList; //주기정보 리스트
}
