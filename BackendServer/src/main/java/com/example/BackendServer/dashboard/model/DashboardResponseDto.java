package com.example.BackendServer.dashboard.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponseDto {
    private Long vehicles;
    private long active;
    private long inactive;
    private long inspect;
}
