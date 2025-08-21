package com.example.device.controller;

import com.example.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceApiController {

  private final DeviceService deviceService;

//  @PostMapping
//  // @Operation(summary = "새로운 디바이스 생성", description = "새로운 디바이스를 생성합니다.")
//  public DeviceEntity create(@Valid @RequestBody DeviceRequest deviceRequest) {
//    return deviceService.create(deviceRequest);
//  }
}
