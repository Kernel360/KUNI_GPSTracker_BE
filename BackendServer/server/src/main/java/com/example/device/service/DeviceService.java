package com.example.device.service;

import com.example.entity.DeviceEntity;
import com.example.repository.DeviceRepository;
import com.example.device.model.DeviceRequest;
import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
import com.example.entity.VehicleEntity;
import com.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final VehicleRepository vehicleRepository; // VehicleEntity 조회용

  @Transactional
  public DeviceEntity create(DeviceRequest deviceRequest) {
    VehicleEntity vehicle = vehicleRepository.findById(deviceRequest.getVehicleId())
        .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

    DeviceEntity device = DeviceEntity.builder()
        .vehicle(vehicle)
        .terminalId(deviceRequest.getTerminalId())
        .mid(deviceRequest.getMid())
        .pv(deviceRequest.getPv())
        .did(deviceRequest.getDid())
        .build();

    return deviceRepository.save(device);
  }
}
