package com.example.BackendServer.Dashboard.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DashboardMapDto {
    private double latitude;
    private double longitude;
    private String status;
}
