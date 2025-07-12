package com.example.BackendServer.vehicle.controller;

import com.example.BackendServer.vehicle.Service.VehicleApiService;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import com.example.BackendServer.vehicle.model.VehicleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class VehicleApiController {

    private final VehicleApiService vehicleApiService;

    /**
     * 차량 등록 api : 차량 정보를 받아 DB에 저장한다.
     *
     * @param dto : vehicleNumber와 vehicleName을 field로 가진다.
     * @return VehicleCreateDto를 반환한다.
     */
    @PostMapping
    public ResponseEntity<VehicleCreateDto> createVehicle(@RequestBody VehicleCreateDto dto) {

        VehicleEntity entity = vehicleApiService.createVehicle(dto);

        VehicleCreateDto result = VehicleCreateDto.builder()
                .vehicleNumber(entity.getVehicleNumber())
                .vehicleName(entity.getType().toString())
                .build();

        return ResponseEntity.ok(result);
    }

}
