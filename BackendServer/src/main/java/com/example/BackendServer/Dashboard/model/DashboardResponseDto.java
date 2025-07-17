package com.example.BackendServer.Dashboard.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DashboardResponseDto {
    private int vehicles;
    private int active;
    private int inactive;
    private int inspect;
}
