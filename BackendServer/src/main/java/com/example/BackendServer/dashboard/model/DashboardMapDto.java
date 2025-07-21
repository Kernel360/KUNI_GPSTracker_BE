package com.example.BackendServer.dashboard.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardMapDto {
    private double latitude;
    private double longitude;
    private String status;
}
