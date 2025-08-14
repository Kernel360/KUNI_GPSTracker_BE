package com.example.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "대시보드 차량 상태 응답 모델")
public class DashboardResponseDto {
    @Schema(description = "전체 차량 수", example = "100")
    private Long vehicles;
    @Schema(description = "운행 중인 차량 수", example = "75")
    private long active;
    @Schema(description = "정비 중인 차량 수", example = "10")
    private long inactive;
    @Schema(description = "점검 중인 차량 수", example = "5")
    private long inspect;
}
