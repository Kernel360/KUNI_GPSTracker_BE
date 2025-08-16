package com.example.location.controller;

import com.example.location.model.VehicleRealtimeInfoDto;
import com.example.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/{vehicleNumber}")
    @Operation(summary = "차량 실시간 정보 조회", description = "차량 번호를 통해 차량의 실시간 정보를 조회합니다. 1초마다 호출하여 다음 GPS 데이터를 받을 수 있습니다.")
    public ResponseEntity<VehicleRealtimeInfoDto> getVehicleRealTimeInfo(
        @PathVariable @Parameter(description = "차량 번호", example = "12가3456") String vehicleNumber
    ) {
        // 🚀 gpsRecordId 제거, vehicleNumber만 전달
        return ResponseEntity.ok(locationService.getVehicleRealtimeInfo(vehicleNumber));
    }
}
