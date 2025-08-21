package com.example.global.Class;

import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum VehicleType {
	MERCEDES,
	FERRARI,
	PORSCHE;

	@JsonCreator
	public static VehicleType from(String value) {
		for (VehicleType type : VehicleType.values()) {
			if (type.name().equalsIgnoreCase(value)) {
				return type;
			}
		}
		throw new CustomException(ErrorCode.INVALID_VEHICLE_TYPE);
	}
}
