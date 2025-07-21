package com.example.BackendServer.dashboard.controller;

import com.example.BackendServer.dashboard.service.DashboardService;
import com.example.BackendServer.dashboard.model.DashboardResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendServer.dashboard.model.DashboardMapDto;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> returnVehicleStatus() {
        DashboardResponseDto dashboardResponseDto = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardResponseDto);
    }

    @GetMapping("/map")
    public ResponseEntity<List<DashboardMapDto>> returnVehicleLocation(
            @RequestParam(required = false) String status
    ) {
        List<DashboardMapDto> dashboardMapDto = dashboardService.getAllVehicleLocation(status);
        return ResponseEntity.ok(dashboardMapDto);
    }
}
