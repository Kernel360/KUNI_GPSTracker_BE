package com.example.BackendServer.gpsRecord.controller;

import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.gpsRecord.model.GpsRecordRequest;
import com.example.BackendServer.gpsRecord.service.GpsRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gps-record")
@RequiredArgsConstructor
public class GpsRecordApiController {

  private final GpsRecordService gpsRecordService;

  @PostMapping
  // @Operation(summary = "새로운 GPS 레코드 생성", description = "새로운 GPS 레코드를 생성합니다.")
  public GpsRecordEntity create(@Valid @RequestBody GpsRecordRequest gpsRecordRequest) {
    return gpsRecordService.create(gpsRecordRequest);
  }
}
