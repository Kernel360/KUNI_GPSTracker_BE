package com.example.BackendServer.global.Class;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VehicleStatus {
	ACTIVE("활동중인 차량"), INACTIVE("비활동중인 차량"), INSPECTING("수리중인 차량");

	private final String description;
}
