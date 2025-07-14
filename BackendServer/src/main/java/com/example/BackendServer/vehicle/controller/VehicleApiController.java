package com.example.BackendServer.vehicle.controller;

import com.example.BackendServer.vehicle.Service.VehicleApiService;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class VehicleApiController {

    private final VehicleApiService vehicleApiService;

    @PostMapping
    public ResponseEntity<VehicleCreateDto> createVehicle(@Valid @RequestBody VehicleCreateDto dto) {

        VehicleEntity entity = vehicleApiService.createVehicle(dto);

        VehicleCreateDto result = VehicleCreateDto.builder()
                .vehicleNumber(entity.getVehicleNumber())
                .vehicleName(entity.getType())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(result);

    }

    @GetMapping
    public ResponseEntity<?> getVehicleList(
        Pageable pageable,
        @RequestParam VehicleEntity.Status status
    ) {
        return ResponseEntity.ok(vehicleApiService.getVehicleList(pageable, status));
    }
}
