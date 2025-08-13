package com.example.dashboard.controller;

import com.example.dashboard.model.DashboardStatusResponseDto;
import com.example.dashboard.service.DashboardService;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/status")
    @Operation(summary = "대시보드 주간 운행 통계 조회",
            description = "어제 포함 과거 7일간의 운행 수를 조회합니다.")
    public ResponseEntity<DashboardStatusResponseDto> getDashboardWeekStatus() {
        return ResponseEntity.ok(dashboardService.getDashboardWeekStatus());
    }
}
