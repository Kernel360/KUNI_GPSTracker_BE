package com.example.BackendServer.device.service;

import com.example.BackendServer.device.db.DeviceEntity;
import com.example.BackendServer.device.db.DeviceRepository;
import com.example.BackendServer.device.model.DeviceRequest;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final VehicleRepository vehicleRepository; // VehicleEntity 조회용

  public DeviceEntity create(DeviceRequest deviceRequest) {
    VehicleEntity vehicle = vehicleRepository.findById(deviceRequest.getVehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle ID"));

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
