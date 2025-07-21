package com.example.BackendServer.record.db;

import com.example.BackendServer.dashboard.model.DayCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecordRepository extends JpaRepository<RecordEntity,Long> {

    @Query(value = """
        select Date(r.on_time) As day,
                count(*) As totalCar
        from record r
        where Date(r.on_time) Between :start And :end
        group by 1
        order by 1
""", nativeQuery = true)
    List<DayCountView> findDailyCount(@Param("start") LocalDate start, @Param("end")LocalDate end);
}
