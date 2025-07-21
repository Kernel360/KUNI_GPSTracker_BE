package com.example.BackendServer.record.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    @Query("""
       SELECT r FROM RecordEntity r
       WHERE (:vehicleNumber IS NULL OR r.vehicle.vehicleNumber LIKE %:vehicleNumber%)
       AND (:startTime IS NULL OR r.onTime >= :startTime)
       AND (:endTime IS NULL OR r.offTime <= :endTime)
       """)
    Page<RecordEntity> searchRecords(@Param("vehicleNumber") String vehicleNumber,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);
}
