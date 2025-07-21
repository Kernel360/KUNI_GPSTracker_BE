package com.example.BackendServer.gpsRecord.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GpsRecordRepository extends JpaRepository<GpsRecordEntity,Long> {

    @Query(value = """
        SELECT gr.vehicle_id, gr.latitude, gr.longitude, gr.status, gr.oTime
        FROM GpsRecord gr
        INNER JOIN (
            SELECT vehicle_id, MAX(oTime) AS max_time
            FROM GpsRecord
            GROUP BY vehicle_id
        ) latest
        ON gr.vehicle_id = latest.vehicle_id AND gr.oTime = latest.max_time
        """, nativeQuery = true)
    List<GpsRecordEntity> findLatestGpsForAllVehicles();
}
