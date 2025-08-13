package com.example.repository;

import com.example.entity.GpsRecordEntity;
import com.example.global.Class.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GpsRecordRepository extends JpaRepository<GpsRecordEntity,Long> {
    @Query("""
              select g
              from GpsRecordEntity g
              where g.record.id = :recordId
              order by g.oTime desc
              limit 1
            """)
    Optional<GpsRecordEntity> findLatest(Long recordId);

    @Query(value = """
            SELECT gr.*
            FROM gpsrecord gr
            INNER JOIN (
                SELECT vehicle_id, MAX(oTime) AS max_time
                FROM gpsrecord
                GROUP BY vehicle_id
            ) latest
            ON gr.vehicle_id = latest.vehicle_id AND gr.oTime = latest.max_time
            WHERE (:status IS NULL OR gr.status = :status)
            """, nativeQuery = true)
    List<GpsRecordEntity> findLatestGpsForAllVehiclesByStatus(@Param("status") VehicleStatus status);

    @Query("SELECT g FROM GpsRecordEntity g WHERE g.record.id = :recordId ORDER BY g.oTime ASC")
    List<GpsRecordEntity> findByRecordIdOrderByOTime(@Param("recordId") Long recordId);

    @Query("""
            SELECT g FROM GpsRecordEntity g 
            WHERE g.record.id = :recordId 
            AND g.id > :gpsRecordId 
            ORDER BY g.id ASC
            LIMIT 1
            """)
    Optional<GpsRecordEntity> findNextGpsRecord(@Param("recordId") Long recordId, @Param("gpsRecordId") Long gpsRecordId);

}
