package com.example.model;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
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