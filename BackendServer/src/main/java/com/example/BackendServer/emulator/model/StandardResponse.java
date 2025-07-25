package com.example.BackendServer.emulator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponse {
  private String rstCd;
  private String rstMsg;
  private String mdn;
}