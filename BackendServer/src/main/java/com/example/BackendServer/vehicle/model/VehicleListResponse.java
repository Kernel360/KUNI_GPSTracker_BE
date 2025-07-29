package com.example.BackendServer.vehicle.model;

import static lombok.AccessLevel.*;

import com.example.BackendServer.global.Class.VehicleStatus;
import com.example.BackendServer.global.Class.VehicleType;
import com.example.BackendServer.vehicle.db.VehicleEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Builder
@Schema(description = "차량 목록 응답 모델")
public class VehicleListResponse {
	@Schema(description = "차량 ID", example = "12가3456")
	private String carNumber;
	@Schema(description = "차량 이름", example = "MERCEDES")
	private VehicleType type;
	@Schema(description = "차량 상태", example = "ACTIVE")
	private VehicleStatus status;
	//TODO : 소수점 자리 표현할 수 있게 VehicleEntity 타입 수정
	@Schema(description = "총 주행 거리", example = "10000")
	private Long totalDist;

	public static VehicleListResponse from(VehicleEntity entity) {
		return VehicleListResponse.builder()
				.carNumber(entity.getVehicleNumber())
				.type(entity.getType())
				.status(entity.getStatus())
				.totalDist(entity.getTotalDist())
				.build();
	}
}
