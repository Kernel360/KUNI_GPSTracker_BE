package com.example.BackendServer.gpsRecord.db;

import com.example.BackendServer.device.db.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsRecordRepository extends JpaRepository<GpsRecordEntity,Long> {
}
