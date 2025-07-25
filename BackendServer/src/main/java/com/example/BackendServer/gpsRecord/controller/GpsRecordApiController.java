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

  @PostMapping("")
  public GpsRecordEntity create(@Valid @RequestBody GpsRecordRequest gpsRecordRequest) {
    return gpsRecordService.create(gpsRecordRequest);
  }
}
