package com.example.BackendServer.device.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<DeviceEntity,Long> {
}
