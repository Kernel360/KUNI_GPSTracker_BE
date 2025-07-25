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

  @PostMapping("")
  public DeviceEntity create(@Valid @RequestBody DeviceRequest deviceRequest) {
    return deviceService.create(deviceRequest);
  }
}
