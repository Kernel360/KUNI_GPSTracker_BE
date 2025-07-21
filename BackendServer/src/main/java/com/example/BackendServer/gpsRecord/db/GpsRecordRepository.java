package com.example.BackendServer.gpsRecord.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GpsRecordRepository extends JpaRepository<GpsRecordEntity, Long> {

    @Query("SELECT g FROM GpsRecordEntity g WHERE g.record.id = :recordId ORDER BY g.oTime ASC")
    List<GpsRecordEntity> findByRecordIdOrderByOTime(@Param("recordId") Long recordId);
}
