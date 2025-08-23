package com.example.Response;

import com.example.entity.GpsRecordEntity;
import com.example.entity.RecordEntity;
import com.example.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "운행일지 상세 조회 응답 DTO")
public class RecordDetailResponse {
    @Schema(description = "차량 번호", example = "123가4567")
    private String vehicleNumber;
    @Schema(description = "차량 종류", example = "AVANTE")
    private VehicleType vehicleName;
    @Schema(description = "운행 시작 시간", example = "2023-10-01T08:00:00")
    private LocalDateTime onTime;
    @Schema(description = "운행 종료 시간", example = "2023-10-01T18:00:00")
    private LocalDateTime offTime;
    @Schema(description = "총 주행 거리", example = "150.5")
    private String sumDist;
    @Schema(description = "운행 시작 지점 위도", example = "37.5665")
    private Double startLat;
    @Schema(description = "운행 시작 지점 경도", example = "126.9780")
    private Double startLng;
    @Schema(description = "운행 종료 지점 위도", example = "37.5665")
    private Double endLat;
    @Schema(description = "운행 종료 지점 경도", example = "126.9780")
    private Double endLng;
    @Schema(description = "운행일지 GPS 기록 목록")
    private List<RecordPointResponse> record;

    public static RecordDetailResponse from(RecordEntity entity,
                                                  Double startLat,
                                                  Double startLng,
                                                  Double endLat,
                                                  Double endLng,
                                                  List<GpsRecordEntity> gpsList) {

        return RecordDetailResponse.builder()
                .vehicleNumber(entity.getVehicle().getVehicleNumber())
                .vehicleName(entity.getVehicle().getType())
                .onTime(entity.getOnTime())
                .offTime(entity.getOffTime())
                .sumDist(entity.getSumDist())
                .startLat(startLat)
                .startLng(startLng)
                .endLat(endLat)
                .endLng(endLng)
                .record(gpsList.stream()
                        .map(RecordPointResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
