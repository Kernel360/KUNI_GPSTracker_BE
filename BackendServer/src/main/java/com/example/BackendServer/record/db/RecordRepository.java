package com.example.BackendServer.record.db;

import com.example.BackendServer.device.db.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<RecordEntity,Long> {
}
