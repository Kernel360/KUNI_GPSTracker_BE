package com.example.dashboard.controller;

import com.example.dashboard.model.DashboardMapDto;
import com.example.dashboard.model.DashboardResponseDto;
import com.example.dashboard.service.DashboardService;
import com.example.dashboard.model.TopVehicleResponseDto;

import com.example.global.Class.VehicleStatus;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {
    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "대시보드 차량 상태 조회", description = "전체 차량의 상태를 조회합니다.")
    public ResponseEntity<DashboardResponseDto> returnVehicleStatus() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

    @Operation(summary = "대시보드 차량 위치 조회", description = "차량 ID 리스트를 통해 차량의 위치를 조회합니다. 차량 ID가 null이면 전체 차량의 1분 전 위치 정보를 반환합니다.", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "차량 위치 조회 성공",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = DashboardMapDto.class)
                            ),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "DashboardMapDto",
                                    value = """
                                        [

                                            {"latitude": 37.111111, "longitude": 123.456789, "status": "ACTIVE", "vehicleNumber": "12가3456", "type": "MERCEDES"},
                                            {"latitude": 37.222222, "longitude": 123.567890, "status": "INACTIVE", "vehicleNumber": "34나5678", "type": "FERRARI"}

                                        ]
                                        """
                            )
                    )
            )
    })
    @GetMapping("/map")
    public ResponseEntity<List<DashboardMapDto>> returnVehicleLocation(
            @RequestParam(required = false) @Parameter(description = "차량 번호 리스트 (null이면 전체 차량)", example = "[\"37허9534\", \"34나5678\"]") List<String> vehicleNumbers
    ) {
        return ResponseEntity.ok(dashboardService.getAllVehicleLocation(vehicleNumbers));
    }

    @Operation(summary = "최근 1주일 운행량 TOP 3 차량 조회", description = "최근 1주일간 운행 횟수가 가장 많은 차량 3대 조회", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "TOP 3 차량 조회 성공",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = TopVehicleResponseDto.class)
                            ),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "TopVehicleResponseDto",
                                    value = """
                                        [
                                          {"vehicleNumber": "12가3456", "driveCount": 14},
                                          {"vehicleNumber": "34나5678", "driveCount": 12},
                                          {"vehicleNumber": "56다9012", "driveCount": 10}
                                        ]
                                        """
                            )
                    )
            )
    })
    @GetMapping("/top-vehicles")
    public ResponseEntity<List<TopVehicleResponseDto>> getWeeklyTopVehicles() {
        List<TopVehicleResponseDto> topVehicles = dashboardService.getWeeklyTopVehicles();
        return ResponseEntity.ok(topVehicles);
    }

}
