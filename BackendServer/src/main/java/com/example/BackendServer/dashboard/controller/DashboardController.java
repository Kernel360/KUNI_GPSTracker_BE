package com.example.BackendServer.dashboard.controller;

import com.example.BackendServer.dashboard.model.DashboardStatusResponseDto;
import com.example.BackendServer.dashboard.service.DashboardService;
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
    public ResponseEntity<DashboardStatusResponseDto> getDashboardWeekStatus() {
        return ResponseEntity.ok(dashboardService.getDashboardWeekStatus());
    }
}
