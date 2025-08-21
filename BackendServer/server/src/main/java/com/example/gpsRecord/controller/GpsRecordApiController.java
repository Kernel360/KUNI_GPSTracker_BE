package com.example.gpsRecord.controller;

import com.example.gpsRecord.service.GpsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gps-record")
@RequiredArgsConstructor
public class GpsRecordApiController {

  private final GpsRecordService gpsRecordService;

//  @PostMapping
//  // @Operation(summary = "새로운 GPS 레코드 생성", description = "새로운 GPS 레코드를 생성합니다.")
//  public GpsRecordEntity create(@Valid @RequestBody GpsRecordRequest gpsRecordRequest) {
//    return gpsRecordService.create(gpsRecordRequest);
//  }
}
