package com.example.BackendServer.device.controller;

import com.example.BackendServer.device.db.DeviceEntity;
import com.example.BackendServer.device.model.DeviceRequest;
import com.example.BackendServer.device.service.DeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceApiController {

  private final DeviceService deviceService;

  @PostMapping
  // @Operation(summary = "새로운 디바이스 생성", description = "새로운 디바이스를 생성합니다.")
  public DeviceEntity create(@Valid @RequestBody DeviceRequest deviceRequest) {
    return deviceService.create(deviceRequest);
  }
}
