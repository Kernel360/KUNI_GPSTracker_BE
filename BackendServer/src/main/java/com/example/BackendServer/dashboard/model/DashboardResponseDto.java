package com.example.BackendServer.dashboard.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class DashboardResponseDto {
    private Long vehicles;
    private long active;
    private long inactive;
    private long inspect;
}
