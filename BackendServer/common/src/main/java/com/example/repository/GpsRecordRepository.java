package com.example.repository;

import com.example.entity.GpsRecordEntity;
import com.example.global.Class.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GpsRecordRepository extends JpaRepository<GpsRecordEntity,Long> {

    @Query("SELECT g FROM GpsRecordEntity g WHERE g.record.id = :recordId ORDER BY g.oTime ASC")
    List<GpsRecordEntity> findByRecordIdOrderByOTime(@Param("recordId") Long recordId);


    @Query("""
            SELECT g FROM GpsRecordEntity g 
            WHERE g.vehicle.vehicleNumber IN :vehicleNumbers 
            AND g.oTime = :targetTime
            """)
    List<GpsRecordEntity> findLatestGpsByVehicleNumbersAndTime(@Param("vehicleNumbers") List<String> vehicleNumbers, @Param("targetTime") LocalDateTime targetTime);

    @Query("""
            SELECT g FROM GpsRecordEntity g 
            WHERE g.oTime = :targetTime
            """)
    List<GpsRecordEntity> findLatestGpsForAllVehiclesByTime(@Param("targetTime") LocalDateTime targetTime);

    // 1분전 의 정보가 없을떄의 처리 필요

    @Query("""
            SELECT g FROM GpsRecordEntity g 
            WHERE g.oTime <= :targetTime
            AND g.id IN (
                SELECT MAX(g2.id) 
                FROM GpsRecordEntity g2 
                WHERE g2.oTime <= :targetTime
                GROUP BY g2.vehicle.id
            )
            """)
    List<GpsRecordEntity> findLatestGpsForAllVehiclesNULL(@Param("targetTime") LocalDateTime targetTime);

    @Query("""
            SELECT g FROM GpsRecordEntity g 
            WHERE g.vehicle.vehicleNumber IN :vehicleNumbers 
            AND g.oTime <= :targetTime
            AND g.id IN (
                SELECT MAX(g2.id) 
                FROM GpsRecordEntity g2 
                WHERE g2.vehicle.vehicleNumber IN :vehicleNumbers 
                AND g2.oTime <= :targetTime
                GROUP BY g2.vehicle.id
            )
            """)
    List<GpsRecordEntity> findLatestGpsByVehicleNumbersNULL(@Param("vehicleNumbers") List<String> vehicleNumbers, @Param("targetTime") LocalDateTime targetTime);

}
