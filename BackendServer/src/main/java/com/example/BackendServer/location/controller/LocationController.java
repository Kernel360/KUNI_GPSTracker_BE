package com.example.BackendServer.location.controller;

import com.example.BackendServer.location.model.VehicleRealtimeInfoDto;
import com.example.BackendServer.location.service.LocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/{vehicleNumber}")
    @Operation(summary = "차량 실시간 정보 조회", description = "차량 번호를 통해 차량의 실시간 정보를 조회합니다.")
    public ResponseEntity<VehicleRealtimeInfoDto> getVehicleRealTimeInfo(@PathVariable @Parameter(description = "차량 이름", example = "12가3456") String vehicleNumber) {
        return ResponseEntity.ok(locationService.getVehicleRealtimeInfo(vehicleNumber));
    }
}
