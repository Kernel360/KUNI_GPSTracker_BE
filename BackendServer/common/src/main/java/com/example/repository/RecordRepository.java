package com.example.repository;

import com.example.entity.RecordEntity;
import com.example.model.DayCountView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    // 📌 여기에 추가
    Optional<RecordEntity> findTopByVehicleIdAndOffTimeIsNullOrderByOnTimeDesc(Long vehicleId);

    @Query(value = """
        select Date(r.on_time) As day,
                count(*) As totalCar
        from record r
        where Date(r.on_time) Between :start And :end
        group by 1
        order by 1
""", nativeQuery = true)
    List<DayCountView> findDailyCount(@Param("start") LocalDate start, @Param("end")LocalDate end);

    Optional<RecordEntity> findTopByVehicleIdOrderByOnTimeDesc(Long vehicleId);

    Optional<RecordEntity> findByVehicleId(Long id);

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
