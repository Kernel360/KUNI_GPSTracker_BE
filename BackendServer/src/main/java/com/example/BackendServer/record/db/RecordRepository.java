package com.example.BackendServer.record.db;

import com.example.BackendServer.dashboard.model.DashboardStatusResponseDto;
import com.example.BackendServer.dashboard.model.DayCountView;
import com.example.BackendServer.device.db.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<RecordEntity,Long> {

    @Query(value = """
        select Date(r.on_time) As day,
                count(*) As totalCar
        from record r
        where r.on_time >= :start
        And r.on_time < :end
        group by Date(r.on_time)
        order by Date(r.on_time)
""", nativeQuery = true)
    List<DayCountView> findDailyCount(@Param("start") LocalDateTime start, @Param("end")LocalDateTime end);
}
