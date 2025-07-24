package com.example.BackendServer.vehicle.controller;

import static org.springframework.data.domain.Sort.Direction.*;

import com.example.BackendServer.global.Class.VehicleStatus;
import com.example.BackendServer.vehicle.Service.VehicleApiService;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import com.example.BackendServer.vehicle.model.VehicleListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Slf4j // 로그 사용을 위해 추가
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleApiController {

    private final VehicleApiService vehicleApiService;

    @PostMapping
    @Operation(summary = "차량 등록", description = "차량을 등록합니다.")
    public ResponseEntity<VehicleCreateDto> createVehicle(@Valid @RequestBody VehicleCreateDto dto) {

        VehicleEntity entity = vehicleApiService.createVehicle(dto);

        VehicleCreateDto result = VehicleCreateDto.builder()
                .vehicleNumber(entity.getVehicleNumber())
                .vehicleName(entity.getType())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 삭제
    @DeleteMapping("/{vehicleNumber}")
    @Operation(summary = "차량 삭제", description = "차량번호로 차량을 삭제합니다.")
    public ResponseEntity<Void> deleteByVehicleNumber(
        @PathVariable @Parameter(description = "차량 이름", example = "12가3456") String vehicleNumber) {
        log.info("차량번호 삭제 요청: {}", vehicleNumber);

        vehicleApiService.deleteByVehicleNumber(vehicleNumber);

        return ResponseEntity.ok().build(); // 200 OK 반환
    }

    @GetMapping
    @Operation(summary = "차량 목록 조회", description = "차량 목록을 조회합니다.")
    public ResponseEntity<Page<VehicleListResponse>> getVehicleList(
        @ParameterObject
        @PageableDefault(size = 10, page = 1, sort = "createDate", direction = DESC)
        Pageable pageable,
        @Parameter(description = "차량 이름", example = "12가3456", required = false)
        @RequestParam String vehicleName,
        @Parameter(description = "차량 상태", example = "ACTIVE")
        @RequestParam VehicleStatus status
    ) {
        return ResponseEntity.ok(vehicleApiService.getVehicleList(pageable, vehicleName, status));
    }
}
