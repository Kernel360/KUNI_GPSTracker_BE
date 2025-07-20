package com.example.BackendServer.location.controller;

import com.example.BackendServer.location.model.VehicleRealtimeInfoDto;
import com.example.BackendServer.location.service.LocationService;
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
    public ResponseEntity<VehicleRealtimeInfoDto> getVehicleRealTimeInfo(@PathVariable String vehicleNumber) {

        VehicleRealtimeInfoDto dto = locationService.getVehicleRealtimeInfo(vehicleNumber);

        return ResponseEntity.ok(dto);
    }
}